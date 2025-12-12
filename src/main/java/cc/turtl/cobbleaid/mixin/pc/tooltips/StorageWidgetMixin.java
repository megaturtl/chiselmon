package cc.turtl.cobbleaid.mixin.pc.tooltips;

import com.cobblemon.mod.common.client.gui.pc.StorageWidget;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.tooltips.StorageSlotTooltipState;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render tooltips in the storage widget.
 * This mixin is kept minimal - it delegates rendering to the feature.
 */
// needs to run after morecobblemontweaks multiselect mixin
@Mixin(value = StorageWidget.class, priority = 2000)
public class StorageWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void cobbleaid$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        CobbleAid mod = CobbleAid.getInstance();
        ModConfig config = mod.getConfig();

        if (config.modDisabled) {
            StorageSlotTooltipState.clear();
            return;
        }
        
        // Delegate to feature
        mod.getPcTooltipsFeature().renderTooltips(context, mouseX, mouseY);
    }
}
