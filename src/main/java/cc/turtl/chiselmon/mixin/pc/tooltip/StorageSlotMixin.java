package cc.turtl.chiselmon.mixin.pc.tooltip;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.pc.StorageSlotTooltipState;
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

    @Shadow(remap = false)
    public abstract boolean isHovered(int mouseX, int mouseY);

    // Track which slot is hovered
    @Inject(method = "renderWidget", at = @At("HEAD"), remap = false)
    private void chiselmon$trackHover(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Chiselmon.isDisabled()) {
            return;
        }

        if (this.isHovered(mouseX, mouseY) && this.getPokemon() != null) {
            StorageSlotTooltipState.setHoveredSlot((StorageSlot) (Object) this, mouseX, mouseY);
        }
    }
}
