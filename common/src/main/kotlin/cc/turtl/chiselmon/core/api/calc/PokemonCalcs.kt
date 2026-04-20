package cc.turtl.chiselmon.core.api.calc

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon

/** Counts how many unique ability templates exist in a Pokemon's potential pool. */
fun countUniqueAbilities(pokemon: Pokemon): Int =
    pokemon.form.abilities.mapping.values
        .flatten()
        .map { it.template }
        .distinct()
        .count()

/** Counts how many IVs are at the maximum value (31). */
fun countPerfectIVs(pokemon: Pokemon): Int =
    Stats.PERMANENT.count { pokemon.ivs.getEffectiveBattleIV(it) == IVs.MAX_VALUE }

/**
 * Calculates the probable moveset of a wild Pokemon.
 *
 * @param preferLatest If true, returns only the last 4 moves learned (typical wild moveset).
 */
fun getPossibleMoves(pokemon: Pokemon, preferLatest: Boolean): Set<MoveTemplate> {
    val allMoves = pokemon.form.moves.getLevelUpMovesUpTo(pokemon.level)

    if (allMoves.isEmpty()) return setOf(Moves.getExceptional())
    if (!preferLatest || allMoves.size <= 4) return allMoves

    return LinkedHashSet(allMoves.toList().takeLast(4))
}