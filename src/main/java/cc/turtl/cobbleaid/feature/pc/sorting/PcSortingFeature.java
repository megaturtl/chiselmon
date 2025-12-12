package cc.turtl.cobbleaid.feature.pc.sorting;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * PC Sorting Feature - Enables quick sorting of Pokemon in PC boxes.
 * <p>
 * This feature adds sorting functionality to the PC GUI, allowing users
 * to quickly organize their Pokemon by various criteria such as Pokedex number,
 * level, name, etc.
 * </p>
 * <p>
 * The sorting logic is implemented in {@link PcSorter} and the UI is
 * handled by {@link PcSortUIHandler} through mixins.
 * </p>
 */
public class PcSortingFeature implements Feature {
    
    @Override
    public void initialize() {
        // Sorting UI is added via PCGUIMixin
        // No additional initialization required
        CobbleAid.getLogger().debug("PC Sorting feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().pc.quickSortEnabled;
    }
    
    @Override
    public String getName() {
        return "PC Sorting";
    }
}
