package cc.turtl.cobbleaid.mixin.pc.tooltips;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to track hovered slots for tooltip rendering.
 * This mixin is kept minimal - it just tracks hover state.
 */
@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow(remap = false)
    public abstract boolean isHovered(int mouseX, int mouseY);

    // Track which slot is hovered
    @Inject(method = "renderWidget", at = @At("HEAD"), remap = false)
    private void cobbleaid$trackHover(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CobbleAid mod = CobbleAid.getInstance();
        ModConfig config = mod.getConfig();
        
        if (config.modDisabled) {
            return;
        }

        if (this.isHovered(mouseX, mouseY) && this.getPokemon() != null) {
            // Delegate to feature
            mod.getPcTooltipsFeature().trackHoveredSlot((StorageSlot) (Object) this, mouseX, mouseY);
        }
    }
}
