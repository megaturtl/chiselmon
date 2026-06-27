package cc.turtl.chiselmon.core.api.filter

import cc.turtl.chiselmon.core.api.predicate.IS_EXTREME_SIZE
import cc.turtl.chiselmon.core.api.predicate.IS_LEGENDARY
import cc.turtl.chiselmon.core.api.predicate.IS_SHINY
import cc.turtl.chiselmon.core.util.normalizeSpeciesName
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.function.Predicate

/**
 * Parses a simple tag string into a [Predicate] for [Pokemon].
 *
 * Supported tags:
 * - "shiny" -- matches shiny pokemon
 * - "legendary" -- matches legendary pokemon
 * - "extreme_size" -- matches pokemon with extreme size
 * - "type=fire" -- matches pokemon with the given type
 * - "species=pikachu" -- matches a specific species
 * - "form=alolan" -- matches alolan forms
 * - "gender=male" -- matches a specific gender
 * - "min_size=1.5" -- matches pokemon at or above the given scale
 * - "max_size=0.4" -- matches pokemon at or below the given scale
 * - "min_level=50" -- matches pokemon at or above the given level
 */
object FilterTagParser {

    @JvmStatic
    fun parse(tag: String): Predicate<Pokemon> {
        return when (val normalized = tag.lowercase().trim()) {
            "shiny" -> IS_SHINY
            "legendary" -> IS_LEGENDARY
            "extreme_size" -> IS_EXTREME_SIZE
            else -> parseComplexTag(normalized)
        }
    }

    private fun parseComplexTag(tag: String): Predicate<Pokemon> {
        if (!tag.contains("=")) return Predicate { false }

        val (key, rawValue) = tag.split("=", limit = 2).let {
            it[0].trim() to it[1].trim()
        }

        return when (key) {
            "species" -> {
                val normalizedSearch = normalizeSpeciesName(rawValue)
                Predicate { pokemon ->
                    normalizeSpeciesName(pokemon.species.name) == normalizedSearch
                }
            }

            "form" -> Predicate { pokemon ->
                pokemon.form.name.equals(rawValue, ignoreCase = true)
            }

            "type" -> Predicate { pokemon ->
                pokemon.types.any { it.name.equals(rawValue, ignoreCase = true) }
            }

            "gender" -> {
                val gender = try {
                    Gender.valueOf(rawValue.uppercase())
                } catch (_: IllegalArgumentException) {
                    throw FilterConditionParser.ParseException(
                        "Invalid gender '$rawValue' -- expected male, female, or genderless"
                    )
                }
                Predicate { it.gender == gender }
            }

            "min_size" -> {
                val threshold = rawValue.toFloatOrNull()
                    ?: throw FilterConditionParser.ParseException("Invalid min_size value: '$rawValue'")
                Predicate { it.scaleModifier >= threshold }
            }

            "max_size" -> {
                val threshold = rawValue.toFloatOrNull()
                    ?: throw FilterConditionParser.ParseException("Invalid max_size value: '$rawValue'")
                Predicate { it.scaleModifier <= threshold }
            }

            "min_level" -> {
                val level = rawValue.toIntOrNull()
                    ?: throw FilterConditionParser.ParseException("Invalid min_level value: '$rawValue'")
                Predicate { it.level >= level }
            }

            else -> Predicate { false }
        }
    }
}
