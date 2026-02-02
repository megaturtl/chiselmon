package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.PCConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IconRenderer {
    // sizes in pixels
    private static final int SRC_SIZE = 9;
    private static final int DEST_SIZE = 5;
    private static final int MAX_ROWS = 3;
    private static final float RENDER_Z = 100.0f;
    private static final int COLUMN_SPACE = 18;

    public static void renderIcons(GuiGraphics context, @NotNull Pokemon pokemon, int x, int y) {

        PCConfig.PcIconConfig config = ChiselmonConstants.CONFIG.pc.icon;

        List<IconEntry> entries = IconRegistry.getEntries();

        if (entries.isEmpty()) return;

        context.pose().pushPose();
        context.pose().translate(x + 1, y + 6, RENDER_Z);

        int count = 0;
        for (IconEntry entry : entries) {
            if (entry.shouldDisplay(config, pokemon)) {
                int col = count / MAX_ROWS;
                int row = count % MAX_ROWS;

                int renderX = col * COLUMN_SPACE;
                int renderY = row * DEST_SIZE;

                renderIcon(context, entry, renderX, renderY);
                count++;
            }
        }

        context.pose().popPose();
    }

    private static void renderIcon(GuiGraphics context, IconEntry entry, int x, int y) {
        context.blit(
                entry.resource(),
                x, y,
                DEST_SIZE, DEST_SIZE,
                0, 0,
                SRC_SIZE, SRC_SIZE,
                SRC_SIZE, SRC_SIZE
        );
    }
}
