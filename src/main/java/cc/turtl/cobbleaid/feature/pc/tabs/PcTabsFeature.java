package cc.turtl.cobbleaid.feature.pc.tabs;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * PC Tabs/Bookmarks Feature - Adds bookmark functionality to PC boxes.
 * <p>
 * This feature allows users to bookmark specific PC boxes for quick access.
 * Bookmarked boxes are displayed as tabs at the top of the PC GUI for easy
 * navigation between frequently used boxes.
 * </p>
 * <p>
 * The tab management is handled by {@link PCTabManager} and the UI
 * components are added via mixins in the tabs package.
 * </p>
 */
public class PcTabsFeature implements Feature {
    
    @Override
    public void initialize() {
        // Tab UI is added via PCGUIMixin
        // Tab data is persisted in WorldDataStore
        // No additional initialization required
        CobbleAid.getLogger().debug("PC Tabs feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().pc.bookmarksEnabled;
    }
    
    @Override
    public String getName() {
        return "PC Tabs";
    }
}
