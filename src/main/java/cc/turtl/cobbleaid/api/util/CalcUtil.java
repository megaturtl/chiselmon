package cc.turtl.cobbleaid.api.util;

import java.util.List;
import java.util.Map;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.pokemon.Species;

public class CalcUtil {
    public static int getSpeciesAbilityCount(Species species) {
        // 1. Get the AbilityPool (which extends PrioritizedList).
        com.cobblemon.mod.common.api.abilities.AbilityPool pool = species.getAbilities();

        // 2. Access the map via the base class method (assuming access to protected
        // member).
        Map<Priority, List<PotentialAbility>> map = pool.getMapping();

        int count = 0;

        // 3. Sum the size of the lists in the map.
        for (List<?> abilities : map.values()) {
            count += abilities.size();
        }

        return count;
    }
}
