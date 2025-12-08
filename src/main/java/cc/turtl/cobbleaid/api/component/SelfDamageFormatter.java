package cc.turtl.cobbleaid.api.component;

import java.util.List;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.filter.SelfDamageHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class SelfDamageFormatter {
    private SelfDamageFormatter() {
    }

    private static final Component SEPARATOR = Component.literal(", ").withColor(ComponentColor.LIGHT_GRAY);

    public static Component format(Pokemon pokemon) {
        List<MoveTemplate> selfDamagingMoves = SelfDamageHelper.getSelfDamagingMoves(pokemon);

        MutableComponent result = Component.empty();

        boolean first = true;
        for (MoveTemplate move : selfDamagingMoves) {
            if (!first)
                result.append(SEPARATOR);
            result.append(move.getDisplayName().withColor(ComponentColor.RED));
            first = false;
        }
        return result;
    }
}