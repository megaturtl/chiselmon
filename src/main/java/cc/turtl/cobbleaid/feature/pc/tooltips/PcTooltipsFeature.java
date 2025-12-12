package cc.turtl.cobbleaid.feature.pc.tooltips;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

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
 * Tooltip rendering is handled through mixins in the tooltips package,
 * with state managed by {@link StorageSlotTooltipState}.
 * </p>
 */
public class PcTooltipsFeature implements Feature {
    
    @Override
    public void initialize() {
        // Tooltip rendering is handled via StorageSlotMixin and StorageWidgetMixin
        // No additional initialization required
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
}
