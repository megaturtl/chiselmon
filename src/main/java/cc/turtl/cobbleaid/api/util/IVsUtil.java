package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public class IVsUtil {

    public static final Stat[] IVS_LIST = {
            Stats.HP, Stats.ATTACK, Stats.DEFENCE,
            Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    };

    public static int numberMaxIvs(IVs ivs) {
        if (ivs == null)
            return 0;

        int maxIvs = 0;

        for (int i = 0; i < IVS_LIST.length; i++) {
            Stat stat = IVS_LIST[i];
            int value = ivs.getEffectiveBattleIV(stat);

            if (value == IVs.MAX_VALUE) {
                maxIvs += 1;
            }
        }
        return maxIvs;
    }
}