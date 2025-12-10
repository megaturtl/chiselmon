package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.tooltip.StorageSlotTooltipState;
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

    ModConfig config = CobbleAid.getInstance().getConfig();

    // Track which slot is hovered
    @Inject(method = "renderWidget", at = @At("HEAD"), remap = false)
    private void cobbleaid$trackHover(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (config.modDisabled || !config.showTooltips) {
            return;
        }
        
        if (this.isHovered(mouseX, mouseY) && this.getPokemon() != null) {
            StorageSlotTooltipState.setHoveredSlot((StorageSlot)(Object)this, mouseX, mouseY);
        }
    }
}