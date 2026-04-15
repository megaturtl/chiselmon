package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.turtlshell.api.core.format.ColorLib;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.WallpapersScrollingWidget;
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cc.turtl.chiselmon.core.util.format.ComponentUtilsKt.createComponent;

@Mixin(value = WallpapersScrollingWidget.WallpaperEntry.class)
public abstract class MixinWallpaperEntry {

    @Unique
    private static final Component HINT_COMPONENT = createComponent("APPLY ALL", ColorLib.INSTANCE.getYELLOW().getRGB(), true);
    @Unique
    private static final float HINT_SCALE = 0.70f;

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
    private void chiselmon$renderBulkHint(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick, CallbackInfo ci) {
        if (ChiselmonConfig.INSTANCE.getGeneral().getModDisabled()) return;
        if (!Screen.hasControlDown()) return;

        guiGraphics.pose().pushPose();

        float x = (left + width) - 49;
        float y = (top + height) - 12;

        guiGraphics.pose().translate(x, y, 100f);
        guiGraphics.pose().scale(HINT_SCALE, HINT_SCALE, 1f);

        guiGraphics.drawString(Minecraft.getInstance().font, HINT_COMPONENT, 0, 0, 0xFFFFFF);

        guiGraphics.pose().popPose();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void chiselmon$handleBulkClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ChiselmonConfig.INSTANCE.getGeneral().getModDisabled()) return;
        if (!Screen.hasControlDown()) return;

        boolean isAlt = Screen.hasShiftDown() && altWallpaper != null;
        ResourceLocation appliedWallpaper = isAlt ? altWallpaper : wallpaper;

        PCGUI pcGui = this.outer.getPcGui();
        int boxCount = pcGui.getPc().getBoxes().size();

        for (int i = 0; i < boxCount; i++) {
            pcGui.getPc().getBoxes().get(i).setWallpaper(appliedWallpaper);
            new RequestChangePCBoxWallpaperPacket(pcGui.getPc().getUuid(), i, wallpaper, isAlt ? altWallpaper : null).sendToServer();
        }

        pcGui.getUnseenWallpapers().remove(this.wallpaper);
        this.isNew = false;

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
        cir.setReturnValue(true);
    }
}