package cc.turtl.chiselmon.core.api.calc

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes

// Type effectiveness chart: [attacker][defender]
private val TYPE_CHART = arrayOf(
    /* Normal    */ floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, .5f, 0f, 1f, 1f, .5f, 1f),
    /* Fire      */ floatArrayOf(1f, .5f, .5f, 2f, 1f, 2f, 1f, 1f, 1f, 1f, 1f, 2f, .5f, 1f, .5f, 1f, 2f, 1f),
    /* Water     */ floatArrayOf(1f, 2f, .5f, .5f, 1f, 1f, 1f, 1f, 2f, 1f, 1f, 1f, 2f, 1f, .5f, 1f, 1f, 1f),
    /* Grass     */ floatArrayOf(1f, .5f, 2f, .5f, 1f, 1f, 1f, .5f, 2f, .5f, 1f, .5f, 2f, 1f, .5f, 1f, .5f, 1f),
    /* Electric  */ floatArrayOf(1f, 1f, 2f, .5f, .5f, 1f, 1f, 1f, 0f, 2f, 1f, 1f, 1f, 1f, .5f, 1f, 1f, 1f),
    /* Ice       */ floatArrayOf(1f, .5f, .5f, 2f, 1f, .5f, 1f, 1f, 2f, 2f, 1f, 1f, 1f, 1f, 2f, 1f, .5f, 1f),
    /* Fighting  */ floatArrayOf(2f, 1f, 1f, 1f, 1f, 2f, 1f, .5f, 1f, .5f, .5f, .5f, 2f, 0f, 1f, 2f, 2f, .5f),
    /* Poison    */ floatArrayOf(1f, 1f, 1f, 2f, 1f, 1f, 1f, .5f, .5f, 1f, 1f, 1f, .5f, .5f, 1f, 1f, 0f, 2f),
    /* Ground    */ floatArrayOf(1f, 2f, 1f, .5f, 2f, 1f, 1f, 2f, 1f, 0f, 1f, .5f, 2f, 1f, 1f, 1f, 2f, 1f),
    /* Flying    */ floatArrayOf(1f, 1f, 1f, 2f, .5f, 1f, 2f, 1f, 1f, 1f, 1f, 2f, .5f, 1f, 1f, 1f, .5f, 1f),
    /* Psychic   */ floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 2f, 2f, 1f, 1f, .5f, 1f, 1f, 1f, 1f, 0f, .5f, 1f),
    /* Bug       */ floatArrayOf(1f, .5f, 1f, 2f, 1f, 1f, .5f, .5f, 1f, .5f, 2f, 1f, 1f, .5f, 1f, 2f, .5f, .5f),
    /* Rock      */ floatArrayOf(1f, 2f, 1f, 1f, 1f, 2f, .5f, 1f, .5f, 2f, 1f, 2f, 1f, 1f, 1f, 1f, .5f, 1f),
    /* Ghost     */ floatArrayOf(0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 1f, 1f, 2f, 1f, .5f, 1f, 1f),
    /* Dragon    */ floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 1f, .5f, 0f),
    /* Dark      */ floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, .5f, 1f, 1f, 1f, 2f, 1f, 1f, 2f, 1f, .5f, 1f, .5f),
    /* Steel     */ floatArrayOf(1f, .5f, .5f, 1f, .5f, 2f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 1f, 1f, 1f, .5f, 2f),
    /* Fairy     */ floatArrayOf(1f, .5f, 1f, 1f, 1f, 1f, 2f, .5f, 1f, 1f, 1f, 1f, 1f, 1f, 2f, 2f, .5f, 1f)
)

private val TYPE_ORDER = listOf(
    ElementalTypes.NORMAL, ElementalTypes.FIRE, ElementalTypes.WATER,
    ElementalTypes.GRASS, ElementalTypes.ELECTRIC, ElementalTypes.ICE,
    ElementalTypes.FIGHTING, ElementalTypes.POISON, ElementalTypes.GROUND,
    ElementalTypes.FLYING, ElementalTypes.PSYCHIC, ElementalTypes.BUG,
    ElementalTypes.ROCK, ElementalTypes.GHOST, ElementalTypes.DRAGON,
    ElementalTypes.DARK, ElementalTypes.STEEL, ElementalTypes.FAIRY
)

private fun typeIndex(type: ElementalType) = TYPE_ORDER.indexOf(type)

/**
 * Computes the effectiveness of all attacking types against the given typing.
 */
fun computeMatchups(defendingTypes: Iterable<ElementalType>): TypingMatchups {
    val results = ElementalTypes.all().associateWith { attackingType ->
        val atkIdx = typeIndex(attackingType)
        if (atkIdx == -1) return@associateWith 1f

        defendingTypes.fold(1f) { multiplier, defendingType ->
            val defIdx = typeIndex(defendingType)
            if (defIdx == -1) multiplier
            else multiplier * TYPE_CHART[atkIdx][defIdx]
        }
    }
    return TypingMatchups(results)
}

/**
 * Maps every attacking type to its damage multiplier against a typing.
 */
data class TypingMatchups(val multiplierMap: Map<ElementalType, Float>) {

    fun getSuperWeak() = filterByMultiplier(4f)
    fun getWeak() = filterByMultiplier(2f)
    fun getResistant() = filterByMultiplier(.5f)
    fun getSuperResistant() = filterByMultiplier(.25f)
    fun getImmune() = filterByMultiplier(0f)

    /** Gets all types that deal more than 1.0x damage, sorted (4x then 2x). */
    fun getAllWeak() = multiplierMap.entries
        .filter { it.value > 1f }
        .sortedByDescending { it.value }
        .map { it.key }

    /** Gets all types that deal less than 1.0x damage, sorted (0x, then 0.25x, then 0.5x). */
    fun getAllResist() = multiplierMap.entries
        .filter { it.value < 1f }
        .sortedBy { it.value }
        .map { it.key }

    private fun filterByMultiplier(target: Float) = multiplierMap
        .filterValues { it == target }
        .keys
        .toList()
}