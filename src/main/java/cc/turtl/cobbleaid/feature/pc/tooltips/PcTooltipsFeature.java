package cc.turtl.cobbleaid.feature.pc.tooltips;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;

/**
 * PC Tooltips Feature - Adds enhanced tooltips to Pokemon in the PC.
 * <p>
 * This feature displays additional information about Pokemon when hovering
 * over their slots in the PC, including:
 * - Basic Pokemon info
 * - Detailed stats (when Shift is held)
 * - IVs, EVs, and other attributes
 * </p>
 * <p>
 * Tooltip rendering is handled through this feature with state managed
 * by {@link StorageSlotTooltipState}.
 * </p>
 */
public class PcTooltipsFeature implements Feature {
    
    private StorageSlotTooltipRenderer renderer;
    
    @Override
    public void initialize() {
        this.renderer = new StorageSlotTooltipRenderer();
        CobbleAid.getLogger().debug("PC Tooltips feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().pc.tooltip.showTooltips;
    }
    
    @Override
    public String getName() {
        return "PC Tooltips";
    }
    
    /**
     * Track a hovered slot for tooltip rendering.
     * This is called from the mixin layer.
     */
    public void trackHoveredSlot(StorageSlot slot, int mouseX, int mouseY) {
        if (isEnabled()) {
            StorageSlotTooltipState.setHoveredSlot(slot, mouseX, mouseY);
        }
    }
    
    /**
     * Render tooltips for the currently hovered slot.
     * This is called from the mixin layer.
     */
    public void renderTooltips(GuiGraphics context, int mouseX, int mouseY) {
        if (renderer != null && isEnabled()) {
            renderer.render(context, mouseX, mouseY);
        }
    }
}
