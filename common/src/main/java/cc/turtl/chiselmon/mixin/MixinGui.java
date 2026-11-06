package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.duck.DuckGui;
import cc.turtl.chiselmon.client.feature.PokeRodOverlay;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui implements DuckGui {
    @Shadow private Component overlayMessageString;
    @Shadow private int overlayMessageTime;

    @Override
    public void chiselmon$clearOverlayMessage(@NotNull Component expected) {
        if (overlayMessageString == expected) overlayMessageTime = 0;
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemNameForPokeRod(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (PokeRodOverlay.render(guiGraphics, 0)) ci.cancel();
    }
}
