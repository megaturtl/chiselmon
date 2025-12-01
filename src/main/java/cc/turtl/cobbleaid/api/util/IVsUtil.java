package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;

import java.util.Arrays;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class IVsUtil {

    public static final Stat[] IVS_LIST = {
            Stats.HP, Stats.ATTACK, Stats.DEFENCE,
            Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    };

    public static MutableComponent getIvsComponent(IVs ivs) {
        if (ivs == null) {
            return Component.literal("N/A").withStyle(ChatFormatting.RED);
        }
        MutableComponent ivsComponent = Component.empty();

        Component slashComponent = Component.literal("/")
                .withStyle(ChatFormatting.DARK_GRAY);

        Arrays.stream(IVS_LIST).forEachOrdered(stat -> {
            int value = ivs.getEffectiveBattleIV(stat);

            // Get ARGB, mask to RGB
            int rgb = ColorUtil.getRatioGradientColor((float) value / (float) IVs.MAX_VALUE) & 0xFFFFFF;

            // 2. Create the styled value component
            Component valueComponent = Component.literal(String.valueOf(value))
                    .withColor(rgb);

            // Collect the values
            if (ivsComponent.getSiblings().isEmpty()) {
                ivsComponent.append(valueComponent);
            } else {
                ivsComponent.append(slashComponent).append(valueComponent);
            }
        });

        float totalIvsPercentage = (float) ivs.getEffectiveBattleTotal() / (float) IVs.MAX_TOTAL;
        int totalRGB = ColorUtil.getRatioGradientColor(totalIvsPercentage) & 0xFFFFFF;

        Component totalIvsComponent = Component.literal(StringUtil.formatPercentage(totalIvsPercentage))
                .withColor(totalRGB);

        return ivsComponent
                .append(" (")
                .append(totalIvsComponent)
                .append(")");
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