package cc.turtl.cobbleaid.feature.pc.tabs;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import net.minecraft.client.gui.components.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * PC Tabs/Bookmarks Feature - Adds bookmark functionality to PC boxes.
 * <p>
 * This feature allows users to bookmark specific PC boxes for quick access.
 * Bookmarked boxes are displayed as tabs at the top of the PC GUI for easy
 * navigation between frequently used boxes.
 * </p>
 * <p>
 * The tab management is handled by {@link PCTabManager} and this feature
 * provides methods for UI integration.
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
    
    /**
     * Create a bookmark button for the PC GUI.
     * This is called from the mixin layer.
     */
    public PCBookmarkButton createBookmarkButton(
            int x, 
            int y, 
            StorageWidget storageWidget,
            Runnable rebuildCallback) {
        
        if (!isEnabled()) {
            return null;
        }
        
        Button.OnPress bookmarkToggle = (button) -> {
            PCTabStore tabStore = CobbleAid.getInstance().getWorldData().getPcTabStore();
            int currentBoxNumber = storageWidget.getBox();
            
            if (tabStore.hasBoxNumber(currentBoxNumber)) {
                tabStore.removeTab(currentBoxNumber);
                CobbleAid.getInstance().saveConfig();
            } else if (tabStore.isFull()) {
                return;
            } else {
                tabStore.addTab(currentBoxNumber);
                CobbleAid.getInstance().saveConfig();
            }
            
            rebuildCallback.run();
        };
        
        return new PCBookmarkButton(x, y, bookmarkToggle);
    }
    
    /**
     * Create tab buttons for the PC GUI.
     * This is called from the mixin layer.
     */
    public List<PCTabButton> createTabButtons(
            StorageWidget storageWidget,
            int tabStartX,
            int tabStartY) {
        
        if (!isEnabled()) {
            return new ArrayList<>();
        }
        
        PCTabStore tabStore = CobbleAid.getInstance().getWorldData().getPcTabStore();
        List<PCTab> tabs = tabStore.getTabs();
        
        return PCTabManager.createTabButtons(storageWidget, tabs, tabStartX, tabStartY);
    }
    
    /**
     * Update bookmark button state based on current box.
     * This is called from the mixin layer.
     */
    public void updateBookmarkButtonState(PCBookmarkButton button, int currentBox) {
        if (!isEnabled() || button == null) {
            return;
        }
        
        PCTabStore tabStore = CobbleAid.getInstance().getWorldData().getPcTabStore();
        boolean isBookmarked = tabStore.hasBoxNumber(currentBox);
        button.setToggled(isBookmarked);
    }
}
