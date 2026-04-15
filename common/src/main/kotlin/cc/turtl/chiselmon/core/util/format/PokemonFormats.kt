package cc.turtl.chiselmon.core.util.format

import cc.turtl.chiselmon.client.api.ClientSpecies
import cc.turtl.chiselmon.client.feature.eggspy.EggDummy
import cc.turtl.chiselmon.client.api.calc.capture.estimateCaptureProbability
import cc.turtl.chiselmon.core.api.calc.computeMatchups
import cc.turtl.chiselmon.core.api.calc.getPossibleMoves
import cc.turtl.chiselmon.core.api.predicate.IS_MARKED
import cc.turtl.chiselmon.core.api.predicate.IS_RIDEABLE
import cc.turtl.chiselmon.core.api.predicate.IS_SELF_DAMAGING
import cc.turtl.turtlshell.api.core.format.ColorLib
import cc.turtl.turtlshell.api.core.format.capitalizeFirst
import cc.turtl.turtlshell.api.core.format.formatPercentage
import cc.turtl.turtlshell.api.core.format.snakeCaseToTitleCase
import cc.turtl.turtlshell.api.core.util.getRatioColor
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.network.chat.Component

// High-level formatter for turning Pokemon data into styled Components.
object PokemonFormats {

    // Icons
    private val ICON_MALE = createComponent("♂", ColorLib.BLUE.rgb)
    private val ICON_FEMALE = createComponent("♀", ColorLib.PINK.rgb)
    private val ICON_GENDERLESS = createComponent("●", ColorLib.LIGHT_GRAY.rgb)

    private val POSITIVE_GRADIENT = ColorLib.Gradients.POSITIVE.rgb()

    private val EGG_GROUP_COLORS = mapOf(
        "monster" to 0x97724C, "water_1" to 0x6BD1F9,
        "bug" to 0xAAC22A, "flying" to 0x90AFF1,
        "field" to 0xE5BA65, "fairy" to 0xFF9EB9,
        "grass" to 0x82D25A, "human_like" to 0x47B7AE,
        "water_3" to 0x2271B4, "mineral" to 0x979067,
        "amorphous" to 0x9F82CC, "water_2" to 0x4B94ED,
        "ditto" to 0xB6AAD5, "dragon" to 0x5E57BF
    )

    // --- Identification ---

    fun genderIcon(gender: Gender?): Component = when (gender) {
        Gender.MALE -> ICON_MALE
        Gender.FEMALE -> ICON_FEMALE
        null -> createComponent("?", ColorLib.DARK_GRAY.rgb)
        else -> ICON_GENDERLESS
    }

    @JvmStatic
    fun detailedName(pokemon: Pokemon, form: Boolean): Component {
        val name = Component.empty()
            .append(genderIcon(pokemon.gender))
            .append(" ")
            .append(createComponent(pokemon.species.name, ColorLib.WHITE.rgb))

        if (form) {
            val formName = pokemon.form.name
            if (!formName.trim().equals("normal", ignoreCase = true)) {
                name.append(createComponent("-$formName", ColorLib.WHITE.rgb))
            }
        }

        name.append(createComponent(" Lv. ${pokemon.level}", ColorLib.LIGHT_GRAY.rgb))

        if (pokemon.shiny) {
            name.append(createComponent(" ★", ColorLib.GOLD.rgb))
        }

        val size = pokemon.scaleModifier
        if (size != 1.0f) {
            name.append(createComponent(" (${"%.2f".format(size)})", ColorLib.TEAL.rgb))
        }

        return name
    }

    fun form(pokemon: Pokemon): Component =
        createComponent(pokemon.form.name, ColorLib.WHITE.rgb)

    // --- Combat & Stats ---

    fun types(pokemon: Pokemon): Component =
        join(pokemon.types, " / ") { type -> type.displayName.withColor(type.hue) }

    fun typingWeaknesses(pokemon: Pokemon): Component {
        val matchups = computeMatchups(pokemon.types)
        val weaknesses = matchups.getAllWeak()

        if (weaknesses.isEmpty()) return NONE

        return join(weaknesses, " / ") { type ->
            val multiplier = matchups.multiplierMap.getOrDefault(type, 1.0f)
            val isSuperWeak = multiplier > 2.0f
            type.displayName.withStyle { style ->
                style.withColor(type.hue).withBold(isSuperWeak)
            }
        }
    }

    @JvmStatic
    fun ivsSummary(pokemon: Pokemon): Component {
        val ivs = pokemon.ivs

        val stats = join(Stats.PERMANENT, "/") { stat ->
            val value = ivs.getEffectiveBattleIV(stat)
            val ratio = value.toFloat() / IVs.MAX_VALUE
            createComponent(value, getRatioColor(ratio, *POSITIVE_GRADIENT))
        }

        val totalRatio = ivs.getEffectiveBattleTotal().toFloat() / IVs.MAX_TOTAL
        val totalColor = getRatioColor(totalRatio, *POSITIVE_GRADIENT)

        return Component.empty()
            .append(stats).append(" ")
            .append(createComponent("(", ColorLib.DARK_GRAY.rgb))
            .append(createComponent(formatPercentage(totalRatio.toDouble()), totalColor))
            .append(createComponent(")", ColorLib.DARK_GRAY.rgb))
    }

    fun selfDamagingMoves(pokemon: Pokemon): Component {
        val moves = getPossibleMoves(pokemon, true)
            .filter(IS_SELF_DAMAGING::test)

        if (moves.isEmpty()) return UNKNOWN

        return join(moves, ", ") { move -> move.displayName.withColor(ColorLib.RED.rgb) }
    }

    // --- Breeding & Species ---

    fun eggGroups(species: ClientSpecies): Component {
        if (species.eggGroups.isEmpty()) return UNKNOWN

        return join(species.eggGroups, " / ") { group ->
            val color = EGG_GROUP_COLORS.getOrDefault(group.lowercase(), ColorLib.WHITE.rgb)
            createComponent(snakeCaseToTitleCase(group), color)
        }
    }

    fun evYield(species: ClientSpecies): Component {
        val yields = species.evYield.entries.filter { it.value > 0 }

        if (yields.isEmpty()) return UNKNOWN

        return join(yields, ", ") { (stat, value) ->
            Component.empty()
                .append(createComponent(value, statColor(stat)))
                .append(" ")
                .append(createComponent(statDisplayName(stat), statColor(stat)))
        }
    }

    // --- Capture ---

    fun catchRate(species: ClientSpecies): Component =
        createComponent(species.catchRate, ColorLib.WHITE.rgb)

    fun catchChance(entity: PokemonEntity, ball: PokeBall): Component {
        val chance = estimateCaptureProbability(entity, ball)
        val color = getRatioColor(chance, *POSITIVE_GRADIENT)

        return Component.empty()
            .append(createComponent("(", ColorLib.LIGHT_GRAY.rgb))
            .append(createComponent(formatPercentage(chance.toDouble()), color))
            .append(createComponent(")", ColorLib.LIGHT_GRAY.rgb))
    }

    // --- Misc ---

    @JvmStatic
    fun rideStyles(pokemon: Pokemon): Component {
        if (!IS_RIDEABLE.test(pokemon)) return UNKNOWN
        val behaviours = pokemon.riding.behaviours ?: return UNKNOWN

        return join(behaviours.keys, ", ") { style ->
            val color = when (style) {
                RidingStyle.LAND -> ColorLib.GREEN.rgb
                RidingStyle.LIQUID -> ColorLib.AQUA.rgb
                // ColorLib has no LAVENDER -- using PURPLE as the closest available.
                // Consider adding LAVENDER (0xB57BFF or similar) to ColorLib if this matters.
                RidingStyle.AIR -> ColorLib.PURPLE.rgb
            }
            createComponent(style.name.capitalizeFirst(), color)
        }
    }

    @JvmStatic
    fun marks(pokemon: Pokemon): Component {
        if (!IS_MARKED.test(pokemon)) return UNKNOWN

        return join(pokemon.marks, ", ") { mark ->
            // Mark.name is not currently exposed via a stable accessor.
            // The translation key is retrieved here via toString(), which for
            // Mark returns the identifier. Update this if the API exposes getName().
            val key = mark.toString()
            val color = mark.titleColour?.toIntOrNull(16) ?: ColorLib.WHITE.rgb

            createComponent(Component.translatable(key).string, color)
                .append(createComponent(" (${formatPercentage(mark.chance.toDouble())})", ColorLib.DARK_GRAY.rgb))
        }
    }

    @JvmStatic
    fun hatchProgress(pokemon: Pokemon): Component {
        val egg = pokemon as? EggDummy ?: return UNKNOWN

        return createComponent("${egg.hatchPercentage}%")
            .append(createComponent(" ("))
            .append(createComponent(egg.stepsRemaining))
            .append(createComponent(" steps)"))
    }

    fun dexStatus(species: Species): Component {
        val dex = CobblemonClient.clientPokedexData
        val knowledge = dex.getHighestKnowledgeForSpecies(species.resourceIdentifier)

        return when (knowledge) {
            PokedexEntryProgress.CAUGHT -> createComponent("Caught", ColorLib.GREEN.rgb)
            PokedexEntryProgress.ENCOUNTERED -> createComponent("Encountered", ColorLib.WHITE.rgb)
            else -> createComponent("Not Encountered", ColorLib.RED.rgb)
        }
    }

    // --- Helpers ---

    private fun statDisplayName(key: String): String = when (key.lowercase()) {
        "hp" -> "HP"
        "attack" -> "Atk"
        "defense", "defence" -> "Def"
        "special_attack" -> "SpA"
        "special_defense", "special_defence" -> "SpD"
        "speed" -> "Spe"
        else -> key.capitalizeFirst()
    }

    private fun statColor(key: String): Int = when (key.lowercase()) {
        "hp" -> ColorLib.GREEN.rgb
        "attack" -> ColorLib.RED.rgb
        "defense", "defence" -> ColorLib.ORANGE.rgb
        "special_attack" -> ColorLib.BLUE.rgb
        "special_defense", "special_defence" -> ColorLib.YELLOW.rgb
        "speed" -> ColorLib.PURPLE.rgb
        else -> ColorLib.WHITE.rgb
    }
}