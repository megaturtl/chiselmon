package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.module.feature.HudModule;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    protected ItemStack lastToolHighlight;

    // Cancel poke rod name rendering and replace with bait info
    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemNameForPokeRod(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Chiselmon.isDisabled()) return;
        ChiselmonConfig config = Chiselmon.services().config().get();

        if (config.showPokeRodBaitAboveHotbar) {
            HudModule module = Chiselmon.modules().getModule(HudModule.class);
            if (module != null && module.shouldRenderPokeRodOverlay(lastToolHighlight)) {
                module.renderPokeRodOverlay(guiGraphics);
                ci.cancel();
            }
        }
    }
}
