package cc.turtl.cobbleaid.feature.pc.icons;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * PC Icon Feature - Displays icons on Pokemon slots in the PC.
 * <p>
 * This feature adds visual indicators to Pokemon slots based on their attributes:
 * - Hidden ability indicator
 * - High IVs indicator
 * - Shiny indicator
 * - Extreme size indicator
 * - Rideable indicator
 * </p>
 * <p>
 * Icons are rendered by {@link PcIconRenderer} and controlled via the
 * PC icons configuration section.
 * </p>
 */
public class PcIconsFeature implements Feature {
    
    @Override
    public void initialize() {
        // Icon rendering is handled by PcIconRenderer through mixins
        // No additional initialization required
        CobbleAid.getLogger().debug("PC Icons feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        // Feature is enabled if any icon is enabled
        var iconConfig = CobbleAid.getInstance().getConfig().pc.icons;
        return iconConfig.hiddenAbility 
            || iconConfig.highIvs 
            || iconConfig.shiny 
            || iconConfig.extremeSize 
            || iconConfig.rideable;
    }
    
    @Override
    public String getName() {
        return "PC Icons";
    }
}
