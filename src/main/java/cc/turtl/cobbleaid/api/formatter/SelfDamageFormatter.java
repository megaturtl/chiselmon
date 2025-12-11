package cc.turtl.cobbleaid.api.formatter;

import java.util.List;
import java.util.Set;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.property.MoveProperties;
import cc.turtl.cobbleaid.api.util.CalcUtil;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class SelfDamageFormatter {
    private SelfDamageFormatter() {
    }

    private static final Component SEPARATOR = Component.literal(", ").withColor(ColorUtil.LIGHT_GRAY);

    public static Component format(Pokemon pokemon) {
        Set<MoveTemplate> possibleMoves = CalcUtil.getPossibleMoves(pokemon, true);
        List<MoveTemplate> possibleSelfDamagingMoves = MoveProperties.IS_SELF_DAMAGING.filter(possibleMoves);

        MutableComponent result = Component.empty();

        boolean first = true;
        for (MoveTemplate move : possibleSelfDamagingMoves) {
            if (!first)
                result.append(SEPARATOR);
            result.append(move.getDisplayName().withColor(ColorUtil.RED));
            first = false;
        }
        return result;
    }
}