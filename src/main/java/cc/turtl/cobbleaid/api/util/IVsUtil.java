package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import net.minecraft.ChatFormatting;

public class IVsUtil {

    private static final String ZERO_IV_COLOR = ChatFormatting.RED.toString();
    private static final String PERFECT_IV_COLOR = ChatFormatting.GREEN.toString();
    private static final String SLASH_COLOR = ChatFormatting.DARK_GRAY.toString();

    private static final String RESET_COLOR = ChatFormatting.RESET.toString();

    private static final String IVS_HEADER = "(HP/Atk/Def/SpA/SpD/Spe)";

    public static final Stat[] IVS_LIST = {
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
            ivsBuilder.append(System.lineSeparator());
        }

        for (int i = 0; i < IVS_LIST.length; i++) {
            Stat stat = IVS_LIST[i];

            int value = ivs.getEffectiveBattleIV(stat);

            if (value == 0) {
                ivsBuilder.append(ZERO_IV_COLOR);
            }

            if (value == IVs.MAX_VALUE) {
                ivsBuilder.append(PERFECT_IV_COLOR);
            }

            ivsBuilder.append(value);

            if (i < IVS_LIST.length - 1) {
                ivsBuilder.append(SLASH_COLOR);
                ivsBuilder.append("/");
                ivsBuilder.append(RESET_COLOR);
            }
        }

        ivsBuilder.append(RESET_COLOR);

        return ivsBuilder.toString();
    }

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