package cc.turtl.cobbleaid.api.formatter;

import java.util.HashMap;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Species;

import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class EVYieldFormatter {
    private EVYieldFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ColorUtil.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal(", ").withColor(ColorUtil.LIGHT_GRAY);

    public static Component format(Species species) {
        HashMap<Stat, Integer> eVMap = species.getEvYield();

        if (eVMap.isEmpty())
            return UNKNOWN;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (Stat stat : Stats.Companion.getPERMANENT()) {
            int value = eVMap.getOrDefault(stat, 0);

            if (value != 0) {
                if (!first)
                    result.append(SEPARATOR);
                result.append(Component.literal(String.valueOf(value)).withColor(ColorUtil.WHITE));
                result.append(Component.literal(" "));
                result.append(stat.getDisplayName()).withColor(ColorUtil.WHITE);
                first = false;
            }
        }

        return result;
    }
}