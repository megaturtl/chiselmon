package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.PCConfig;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TooltipBuilder {
    public static Tooltip buildPCTooltip(Pokemon pokemon, boolean shiftDown) {

        PCConfig.PCTooltipConfig config = ChiselmonConstants.CONFIG.pc.tooltip;

        MutableComponent content = PokemonFormats.detailedName(pokemon).copy();

        boolean shouldExtend = config.extendOnShift && shiftDown;

        if (shouldExtend) {
            for (TooltipEntry entry : TooltipRegistry.getEntries()) {
                if (entry.shouldDisplay(config, pokemon)) {
                    content.append(Component.literal("\n")).append(entry.getComponent(pokemon));
                }
            }
        }

        return Tooltip.create(content);
    }
}
