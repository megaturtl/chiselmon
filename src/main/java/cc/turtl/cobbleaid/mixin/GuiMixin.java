package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.item.interactive.PokerodItem;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add custom HUD rendering.
 * This mixin is kept minimal - it just hooks into the render method
 * and delegates to feature implementations.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    protected ItemStack lastToolHighlight;

    // Cancel poke rod name rendering and replace with bait info
    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemNameForPokeRod(GuiGraphics guiGraphics, CallbackInfo ci) {
        ModConfig config = CobbleAid.getInstance().getConfig();
        
        // Check if mod is globally disabled first
        if (config.modDisabled) {
            return;
        }
        
        // Delegate to feature - it handles enabled check and rendering
        if (CobbleAid.getInstance().getHudFeature().renderPokeRodOverlay(guiGraphics, lastToolHighlight)) {
            ci.cancel();
        }
    }
}