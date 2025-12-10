package cc.turtl.cobbleaid.feature.gui.pc.tooltip;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;

public class StorageSlotTooltipState {
    private static StorageSlot hoveredSlot = null;
    private static int tooltipMouseX = 0;
    private static int tooltipMouseY = 0;
    
    public static void setHoveredSlot(StorageSlot slot, int mouseX, int mouseY) {
        hoveredSlot = slot;
        tooltipMouseX = mouseX;
        tooltipMouseY = mouseY;
    }
    
    public static StorageSlot getHoveredSlot() {
        return hoveredSlot;
    }
    
    public static int getTooltipMouseX() {
        return tooltipMouseX;
    }
    
    public static int getTooltipMouseY() {
        return tooltipMouseY;
    }
    
    public static void clear() {
        hoveredSlot = null;
    }
}