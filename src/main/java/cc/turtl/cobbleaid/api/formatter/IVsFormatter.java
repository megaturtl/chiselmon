package cc.turtl.cobbleaid.api.formatter;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;

import cc.turtl.cobbleaid.api.util.ColorUtil;
import cc.turtl.cobbleaid.api.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class IVsFormatter {
    private IVsFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ColorUtil.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal("/").withColor(ColorUtil.LIGHT_GRAY);

    public static Component format(IVs ivs) {
        if (ivs == null)
            return UNKNOWN;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (Stat stat : Stats.Companion.getPERMANENT()) {
            int value = ivs.getEffectiveBattleIV(stat);
            int rgb = ColorUtil.getRatioGradientColor((float) value / IVs.MAX_VALUE) & 0xFFFFFF;

            if (!first)
                result.append(SEPARATOR);
            result.append(Component.literal(String.valueOf(value)).withColor(rgb));
            first = false;
        }

        float totalPercent = (float) ivs.getEffectiveBattleTotal() / IVs.MAX_TOTAL;
        int totalRGB = ColorUtil.getRatioGradientColor(totalPercent) & 0xFFFFFF;

        return result
                .append(" (")
                .append(Component.literal(StringUtil.formatPercentage(totalPercent)).withColor(totalRGB))
                .append(")");
    }
}