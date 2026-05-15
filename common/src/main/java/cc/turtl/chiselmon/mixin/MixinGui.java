package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.PokeRodOverlay;
import com.cobblemon.mod.common.item.interactive.PokerodItem;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Shadow
    private ItemStack lastToolHighlight;

    // Cancel vanilla item name rendering
    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemNameForPokeRod(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (lastToolHighlight.getItem() instanceof PokerodItem) {
            PokeRodOverlay.render(guiGraphics);
            ci.cancel();
        }
    }
}