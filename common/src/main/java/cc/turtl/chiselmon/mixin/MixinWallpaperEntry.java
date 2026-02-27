package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.wallpaper.WallpaperManager;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.WallpapersScrollingWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WallpapersScrollingWidget.WallpaperEntry.class)
public abstract class MixinWallpaperEntry {

    @Shadow(aliases = "this$0", remap = false)
    @Final
    private WallpapersScrollingWidget outer;
    @Shadow(remap = false)
    @Final
    private ResourceLocation wallpaper;
    @Shadow(remap = false)
    private ResourceLocation altWallpaper;
    @Shadow(remap = false)
    private boolean isNew;

    @Inject(method = "render", at = @At("RETURN"))
    private void chiselmon$renderBulkHint(GuiGraphics context, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick, CallbackInfo ci) {
        if (ChiselmonConfig.get().general.modDisabled) return;

        WallpaperManager.renderBulkHint(context, left, top, width, height);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void chiselmon$handleBulkClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ChiselmonConfig.get().general.modDisabled) return;

        PCGUI pcGui = this.outer.getPcGui();
        if (WallpaperManager.handleBulkClick(pcGui, this.wallpaper, this.altWallpaper)) {

            // Remove from unseen if we handle the bulk click for the first time the wallpaper is applied
            pcGui.getUnseenWallpapers().remove(this.wallpaper);
            this.isNew = false;

            cir.setReturnValue(true);
        }
    }
}