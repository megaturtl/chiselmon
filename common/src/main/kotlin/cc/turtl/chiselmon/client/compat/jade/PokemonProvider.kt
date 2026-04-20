package cc.turtl.chiselmon.client.compat.jade

import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.core.api.predicate.HAS_SELF_DAMAGING_MOVE
import cc.turtl.chiselmon.core.util.format.PokemonFormats
import cc.turtl.chiselmon.core.util.format.SPACE
import cc.turtl.chiselmon.core.util.format.labelled
import cc.turtl.chiselmon.core.util.modResource
import cc.turtl.turtlshell.api.core.format.ColorLib
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.pokeball.PokeBall
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IEntityComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.impl.ui.HealthElement

/**
 * Jade tooltip provider for Pokemon entities.
 * Displays customizable information based on config settings.
 */
object PokemonProvider : IEntityComponentProvider {

    val POKEDEX_STATUS: ResourceLocation = modResource("pokemon_entity.pokedex_status")
    val TYPING: ResourceLocation = modResource("pokemon_entity.typing")
    val WEAKNESSES: ResourceLocation = modResource("pokemon_entity.weaknesses")
    val FORM: ResourceLocation = modResource("pokemon_entity.form")
    val EGG_GROUPS: ResourceLocation = modResource("pokemon_entity.egg_groups")
    val EV_YIELD: ResourceLocation = modResource("pokemon_entity.ev_yield")
    val CATCH_RATE: ResourceLocation = modResource("pokemon_entity.catch_rate")
    val SELF_DAMAGE_WARNING: ResourceLocation = modResource("pokemon_entity.self_damage_warning")

    private val UID: ResourceLocation = modResource("pokemon_entity")

    override fun getUid(): ResourceLocation = UID

    override fun appendTooltip(tooltip: ITooltip, accessor: EntityAccessor, config: IPluginConfig) {
        if (ChiselmonConfig.general.modDisabled) return
        val entity = accessor.entity as? PokemonEntity ?: return

        val pokemon = entity.pokemon
        val species = pokemon.species


        tooltip.clear()
        tooltip.add(PokemonFormats.detailedName(pokemon, false))
        tooltip.add(HealthElement(entity.maxHealth, entity.health))

        addIfEnabled(tooltip, config, POKEDEX_STATUS, "chiselmon.ui.label.pokedex_status") {
            PokemonFormats.dexStatus(
                species
            )
        }
        addIfEnabled(tooltip, config, TYPING, "chiselmon.ui.label.type") { PokemonFormats.types(pokemon) }
        addIfEnabled(tooltip, config, WEAKNESSES, "chiselmon.ui.label.weaknesses") {
            PokemonFormats.typingWeaknesses(
                pokemon
            )
        }
        addIfEnabled(tooltip, config, FORM, "chiselmon.ui.label.form") { PokemonFormats.form(pokemon) }

        val clientSpecies = ClientSpeciesRegistry.getSpecies(species.name) ?: return
        addIfEnabled(tooltip, config, EGG_GROUPS, "chiselmon.ui.label.egg_groups") {
            PokemonFormats.eggGroups(
                clientSpecies
            )
        }
        addIfEnabled(tooltip, config, EV_YIELD, "chiselmon.ui.label.ev_yield") { PokemonFormats.evYield(clientSpecies) }

        if (config.get(CATCH_RATE)) {
            tooltip.add(
                labelled(
                    Component.translatable("chiselmon.ui.label.catch_rate"),
                    PokemonFormats.catchRate(clientSpecies)
                )
            )
            findHeldPokeball(accessor.player)?.let { ball ->
                tooltip.append(SPACE)
                tooltip.append(PokemonFormats.catchChance(entity, ball))
            }
        }

        if (config.get(SELF_DAMAGE_WARNING) && HAS_SELF_DAMAGING_MOVE.test(pokemon)) {
            tooltip.add(Component.literal("⚠ ").withColor(ColorLib.RED.rgb))
            tooltip.append(PokemonFormats.selfDamagingMoves(pokemon))
        }
    }

    private fun addIfEnabled(
        tooltip: ITooltip,
        config: IPluginConfig,
        key: ResourceLocation,
        labelKey: String,
        value: () -> Component
    ) {
        if (config.get(key)) {
            tooltip.add(labelled(Component.translatable(labelKey), value()))
        }
    }

    private fun findHeldPokeball(player: Player): PokeBall? {
        (player.mainHandItem.item as? PokeBallItem)?.let { return it.pokeBall }
        (player.offhandItem.item as? PokeBallItem)?.let { return it.pokeBall }
        return null
    }
}