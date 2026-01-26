package cc.turtl.chiselmon.api.util;

import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PokemonCalcUtil {

    public static int countUniqueAbilities(Pokemon pokemon) {
        AbilityPool pool = pokemon.getForm().getAbilities();

        return (int) pool.getMapping().values().stream()
                .flatMap(List::stream)
                // Map every PotentialAbility (Hidden or Common) to its core template.
                .map(PotentialAbility::getTemplate)
                // Filter out duplicates based on the template.
                .distinct()
                // Return the count of unique templates.
                .count();
    }

    public static final int countPerfectIVs(Pokemon pokemon) {
        IVs ivs = pokemon.getIvs();
        long perfectCount = Stats.Companion.getPERMANENT().stream()
                // Map each Stat object to its effective IV value
                .mapToInt(ivs::getEffectiveBattleIV)
                // Filter the stream to only keep values equal to 31 (MAX_VALUE)
                .filter(value -> value == IVs.MAX_VALUE)
                // Count the remaining perfect IVs
                .count();

        return (int) perfectCount;
    }

    /**
     * Calculates the probable moveset of a wild Pokemon based on Cobblemon's server initialization logic.
     * This is safe to run on the client as it only uses synchronized Pokemon data (level and form).
     *
     * @param pokemon The Pokemon object.
     * @param preferLatest If true, selects the 4 most recently learned moves. If false, returns all possible level-up moves.
     * @return A Set of MoveTemplate objects representing the selected moves.
     */
    public static Set<MoveTemplate> getPossibleMoves(Pokemon pokemon, boolean preferLatest) {
        // Get all possible level-up moves up to the current level
        Set<MoveTemplate> possibleMoves = pokemon.getForm().getMoves().getLevelUpMovesUpTo(pokemon.getLevel());

        // Check for the "exceptional move" case (if no level-up moves exist)
        if (possibleMoves.isEmpty()) {
            return Set.of(Moves.getExceptional());
        }

        // If preferLatest is false, return all possible moves
        if (!preferLatest) {
            return possibleMoves;
        }

        // If preferLatest is true, select the 4 most recently learned moves
        List<MoveTemplate> moveList = new ArrayList<>(possibleMoves);
        int startIndex = Math.max(0, moveList.size() - 4);
        return new LinkedHashSet<>(moveList.subList(startIndex, moveList.size()));
    }
}
