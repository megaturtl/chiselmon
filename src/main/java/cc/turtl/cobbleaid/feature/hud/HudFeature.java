package cc.turtl.cobbleaid.feature.hud;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import com.cobblemon.mod.common.item.interactive.PokerodItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * HUD Features - Displays various HUD elements.
 * <p>
 * This feature manages HUD overlays including:
 * - PokeRod bait display above hotbar
 * - Spawn tracker (future feature)
 * </p>
 * <p>
 * HUD rendering is handled by this feature through
 * client-side rendering events.
 * </p>
 */
public class HudFeature implements Feature {
    
    private PokeRodBaitOverlay baitOverlay;
    
    @Override
    public void initialize() {
        this.baitOverlay = new PokeRodBaitOverlay();
        CobbleAid.getLogger().debug("HUD feature initialized");
    }
    
    @Override
    public boolean isEnabled() {
        // Feature is enabled if any HUD element is enabled
        return CobbleAid.getInstance().getConfig().showPokeRodBaitAboveHotbar;
    }
    
    @Override
    public String getName() {
        return "HUD Elements";
    }
    
    /**
     * Render the PokeRod bait overlay if holding a PokeRod.
     * This is called from the mixin layer.
     * 
     * @param guiGraphics the graphics context
     * @param heldItem the item being held
     * @return true if the overlay was rendered (should cancel default rendering)
     */
    public boolean renderPokeRodOverlay(GuiGraphics guiGraphics, ItemStack heldItem) {
        if (baitOverlay != null && isEnabled() && heldItem.getItem() instanceof PokerodItem) {
            baitOverlay.render(guiGraphics);
            return true;
        }
        return false;
    }
}
