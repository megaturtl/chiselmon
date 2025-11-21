package cc.turtl.cobbleaid.api;

import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class StatCalculator {
    public static Integer calculateTotalIVs(Pokemon pokemon) {
        if (pokemon == null) return null;
        
        IVs ivs = pokemon.getIvs();
        if (ivs == null) return 0;
    
        int sum = 0;
        for (var entry : ivs) {
            sum += entry.getValue();
        }
        return sum;
    }
}
