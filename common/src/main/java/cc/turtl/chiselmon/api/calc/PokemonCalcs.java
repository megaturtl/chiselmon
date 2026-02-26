package cc.turtl.chiselmon.api.calc;

import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class PokemonCalcs {
    private PokemonCalcs() {
    }

    /**
     * Counts how many unique ability templates exist in a Pokemon's potential pool.
     */
    public static int countUniqueAbilities(Pokemon pokemon) {
        return (int) pokemon.getForm().getAbilities().getMapping().values().stream()
                .flatMap(Collection::stream)
                .map(PotentialAbility::getTemplate)
                .distinct()
                .count();
    }

    /**
     * Counts how many IVs are at the maximum value (31).
     */
    public static int countPerfectIVs(Pokemon pokemon) {
        IVs ivs = pokemon.getIvs();
        return (int) Stats.Companion.getPERMANENT().stream()
                .filter(stat -> ivs.getEffectiveBattleIV(stat) == IVs.MAX_VALUE)
                .count();
    }

    /**
     * Calculates the probable moveset of a wild Pokemon.
     *
     * @param preferLatest If true, returns only the last 4 moves learned (typical wild moveset).
     * @return A Set of MoveTemplate objects.
     */
    public static Set<MoveTemplate> getPossibleMoves(Pokemon pokemon, boolean preferLatest) {
        Set<MoveTemplate> allMoves = pokemon.getForm().getMoves().getLevelUpMovesUpTo(pokemon.getLevel());

        if (allMoves.isEmpty()) {
            return Set.of(Moves.getExceptional());
        }

        if (!preferLatest || allMoves.size() <= 4) {
            return allMoves;
        }

        // Efficiently grab the last 4 items without creating an intermediate ArrayList
        return allMoves.stream()
                .skip(allMoves.size() - 4)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}