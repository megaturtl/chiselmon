package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.config.category.PCConfig;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public final class TooltipBuilder {
    private TooltipBuilder() {
    }

    public static Tooltip build(@NotNull Pokemon pokemon, @NotNull PCConfig.TooltipConfig config, boolean shiftDown) {

        MutableComponent content = PokemonFormats.detailedName(pokemon).copy();

        if (shiftDown) {
            for (TooltipEntry entry : TooltipRegistry.getEntries()) {
                if (entry.shouldDisplay(config, pokemon)) {
                    content.append(Component.literal("\n")).append(entry.getComponent(pokemon));
                }
            }
        }

        return Tooltip.create(content);
    }
}
