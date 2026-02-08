package cc.turtl.chiselmon.feature.pc.wallpaper;

import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class WallpaperManager {
    private static final Component HINT_COMPONENT = ComponentUtils.createComponent("APPLY ALL", ColorUtils.YELLOW, true);
    private static final float HINT_SCALE = 0.70f;

    public static void renderBulkHint(GuiGraphics context, int left, int top, int width, int height) {
        if (!Screen.hasControlDown()) return;

        context.pose().pushPose();

        float x = (left + width) - 49;
        float y = (top + height) - 12;

        context.pose().translate(x, y, 100f);
        context.pose().scale(HINT_SCALE, HINT_SCALE, 1f);

        context.drawString(
                Minecraft.getInstance().font,
                HINT_COMPONENT,
                0, 0, 0xFFFFFF);

        context.pose().popPose();
    }

    public static boolean handleBulkClick(PCGUI pcGui, ResourceLocation wallpaper, ResourceLocation altWallpaper) {
        if (!Screen.hasControlDown()) return false;

        boolean isAlt = Screen.hasShiftDown() && altWallpaper != null;
        ResourceLocation appliedWallpaper = isAlt ? altWallpaper : wallpaper;

        int boxCount = pcGui.getPc().getBoxes().size();

        for (int i = 0; i < boxCount; i++) {
            // Client update
            pcGui.getPc().getBoxes().get(i).setWallpaper(appliedWallpaper);

            // Server update
            new RequestChangePCBoxWallpaperPacket(
                    pcGui.getPc().getUuid(),
                    i,
                    wallpaper,
                    isAlt ? altWallpaper : null
            ).sendToServer();
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
        return true;
    }
}