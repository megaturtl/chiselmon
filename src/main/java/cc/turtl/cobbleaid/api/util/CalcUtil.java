package cc.turtl.cobbleaid.api.util;

import java.util.List;
import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.pokemon.Species;

public class CalcUtil {
    public static int countUniqueAbilities(Species species) {
        AbilityPool pool = species.getAbilities();
        
        return (int) pool.getMapping().values().stream()
                .flatMap(List::stream)
                // Map every PotentialAbility (Hidden or Common) to its core template.
                .map(PotentialAbility::getTemplate)
                // Filter out duplicates based on the template.
                .distinct()
                // Return the count of unique templates.
                .count();
    }
}
