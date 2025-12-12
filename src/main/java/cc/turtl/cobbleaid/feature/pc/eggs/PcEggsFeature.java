package cc.turtl.cobbleaid.feature.pc.eggs;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * PC Egg Preview Feature - Shows egg preview in PC slots.
 * <p>
 * This feature integrates with Neo Daycare to display egg previews
 * in PC slots, allowing users to see what Pokemon will hatch from eggs
 * before they hatch.
 * </p>
 * <p>
 * The egg rendering is handled by {@link PcEggRenderer} through mixins.
 * </p>
 */
public class PcEggsFeature implements Feature {
    
    @Override
    public void initialize() {
        // Egg rendering is handled via mixins in the neodaycare package
        // No additional initialization required
        CobbleAid.getLogger().debug("PC Eggs feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().pc.showEggPreview;
    }
    
    @Override
    public String getName() {
        return "PC Egg Preview";
    }
}
