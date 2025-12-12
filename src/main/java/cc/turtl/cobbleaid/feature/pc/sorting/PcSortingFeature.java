package cc.turtl.cobbleaid.feature.pc.sorting;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SortPCBoxPacket;

/**
 * PC Sorting Feature - Enables quick sorting of Pokemon in PC boxes.
 * <p>
 * This feature adds sorting functionality to the PC GUI, allowing users
 * to quickly organize their Pokemon by various criteria such as Pokedex number,
 * level, name, etc.
 * </p>
 * <p>
 * The sorting logic is implemented in {@link PcSorter} and the UI is
 * handled by {@link PcSortUIHandler}.
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
    
    /**
     * Initialize sorting buttons in the PC GUI.
     * This is called from the mixin layer.
     */
    public void initializeSortButtons(
            PCGUI screen,
            ClientPC pc,
            StorageWidget storage,
            PcSortUIHandler.ButtonAdder adder,
            int screenWidth,
            int screenHeight,
            int baseWidth,
            int baseHeight) {
        
        if (isEnabled()) {
            PcSortUIHandler.initializeSortButtons(
                screen, pc, storage, adder,
                screenWidth, screenHeight, baseWidth, baseHeight
            );
        }
    }
    
    /**
     * Handle middle mouse click for quick sort.
     * This is called from the mixin layer.
     * 
     * @return true if the click was handled
     */
    public boolean handleQuickSortClick(int button, StorageWidget storageWidget, ClientPC pc) {
        if (!isEnabled() || button != 2) { // middle mouse button
            return false;
        }
        
        if (storageWidget != null) {
            storageWidget.resetSelected();
        }
        
        new SortPCBoxPacket(
            pc.getUuid(),
            storageWidget.getBox(),
            CobbleAid.getInstance().getConfig().pc.quickSortMode,
            net.minecraft.client.gui.screens.Screen.hasShiftDown()
        ).sendToServer();
        
        return true;
    }
}
