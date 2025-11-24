package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import net.minecraft.ChatFormatting;

public class IVsUtil {

    private static final String PERFECT_IV_COLOR = ChatFormatting.GREEN.toString();
    private static final String RESET_COLOR = ChatFormatting.RESET.toString();

    private static final String IVS_HEADER = "(HP/Atk/Def/SpA/SpD/Spe)";

    private static final Stat[] IV_DISPLAY_ORDER = {
            Stats.HP, Stats.ATTACK, Stats.DEFENCE,
            Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    };

    public static String getIvsString(IVs ivs) {
        return getIvsString(ivs, false);
    }

    public static String getIvsString(IVs ivs, boolean header) {
        if (ivs == null) {
            return "N/A";
        }

        StringBuilder ivsBuilder = new StringBuilder();
        if (header) {
            ivsBuilder.append(IVS_HEADER);
        }

        for (int i = 0; i < IV_DISPLAY_ORDER.length; i++) {
            Stat stat = IV_DISPLAY_ORDER[i];

            int value = ivs.getEffectiveBattleIV(stat);

            if (value == IVs.MAX_VALUE) {
                ivsBuilder.append(PERFECT_IV_COLOR);
            }

            ivsBuilder.append(value);

            if (i < IV_DISPLAY_ORDER.length - 1) {
                ivsBuilder.append(RESET_COLOR);
                ivsBuilder.append("/");
            }
        }

        ivsBuilder.append(RESET_COLOR);

        ivsBuilder.append(" (Total: ").append(calculateTotalIVs(ivs)).append(")");

        return ivsBuilder.toString();
    }

    public static Integer calculateTotalIVs(IVs ivs) {
        if (ivs == null) return 0;
    
        int sum = 0;
        for (var entry : ivs) {
            sum += entry.getValue();
        }
        return sum;
    }
}