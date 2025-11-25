package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.gui.pc.PcIconRenderer;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {
    @Shadow
    public abstract Pokemon getPokemon();

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1), remap = false)
    private void cobbleaid$renderIcons(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        PcIconRenderer.renderIcons(context, getPokemon(), posX, posY);
    }
}