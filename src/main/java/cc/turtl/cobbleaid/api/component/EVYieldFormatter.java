package cc.turtl.cobbleaid.api.component;

import java.util.HashMap;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Species;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class EVYieldFormatter {
    private EVYieldFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ComponentColor.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal(", ").withColor(ComponentColor.LIGHT_GRAY);
    private static final Stat[] EV_STATS = {
            Stats.HP, Stats.ATTACK, Stats.DEFENCE,
            Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
    };

    public static Component format(Species species) {
        HashMap<Stat, Integer> eVMap = species.getEvYield();

        if (eVMap.isEmpty())
            return UNKNOWN;

        MutableComponent result = Component.empty();
        boolean first = true;

        for (Stat stat : EV_STATS) {
            int value = eVMap.getOrDefault(stat, 0);

            if (value != 0) {
                if (!first)
                    result.append(SEPARATOR);
                result.append(Component.literal(String.valueOf(value)).withColor(ComponentColor.WHITE));
                result.append(Component.literal(" "));
                result.append(stat.getDisplayName()).withColor(ComponentColor.WHITE);
                first = false;
            }
        }

        return result;
    }
}