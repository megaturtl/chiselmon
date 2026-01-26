package cc.turtl.chiselmon.mixin.pc.wallpaper;

import cc.turtl.chiselmon.Chiselmon;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.WallpapersScrollingWidget;
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WallpapersScrollingWidget.WallpaperEntry.class, remap = false)
public abstract class WallpaperEntryMixin {

    // Shadow the outer class instance
    @Shadow(aliases = "this$0")
    @Final
    private WallpapersScrollingWidget outer;

    // Belong to inner class
    @Shadow
    @Final
    private ResourceLocation wallpaper;
    @Shadow
    private ResourceLocation altWallpaper;
    @Shadow
    private boolean isNew;

    @Inject(method = "render", at = @At("RETURN") // Draw AFTER the wallpaper so text is on top
    )
    private void chiselmon$renderBulkOverlay(
            GuiGraphics guiGraphics,
            int index, int top, int left, int width, int height,
            int mouseX, int mouseY,
            boolean hovering, float partialTick,
            CallbackInfo ci) {
        if (Chiselmon.isDisabled()) {
            return;
        }
        if (Screen.hasControlDown()) {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();

            float scale = 0.5F;
            double xPos = (left + width) - 30;
            double yPos = (top + height) - 10;

            pose.translate(xPos, yPos, 200.0f); // Z=200 ensures it renders above the image
            pose.scale(scale, scale, scale);

            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal("SET ALL").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW),
                    0, 0, 0xFFFFFF,
                    true);

            pose.popPose();
        }
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void chiselmon$bulkWallpaperClick(
            double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        if (Chiselmon.isDisabled())
            return;
        if (!Screen.hasControlDown())
            return;

        Minecraft mc = Minecraft.getInstance();
        PCGUI pcGui = this.outer.getPcGui();

        // Determine which wallpaper to apply locally (Normal vs Shift/Alt)
        ResourceLocation appliedWallpaperLocal = Screen.hasShiftDown()
                ? (altWallpaper != null ? altWallpaper : wallpaper)
                : wallpaper;

        int totalBoxes = pcGui.getPc().getBoxes().size();

        for (int i = 0; i < totalBoxes; i++) {
            // 1. Client-side visual update
            pcGui.getPc().getBoxes().get(i).setWallpaper(appliedWallpaperLocal);

            // 2. Send packet to server
            new RequestChangePCBoxWallpaperPacket(
                    pcGui.getPc().getUuid(),
                    i,
                    this.wallpaper,
                    Screen.hasShiftDown() ? this.altWallpaper : null).sendToServer();
        }

        // GUI Cleanup
        pcGui.getUnseenWallpapers().remove(wallpaper);
        this.isNew = false;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));

        // Stop the original method from running
        cir.setReturnValue(true);
        cir.cancel();
    }
}
