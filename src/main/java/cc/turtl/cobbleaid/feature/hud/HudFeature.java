package cc.turtl.cobbleaid.feature.hud;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * HUD Features - Displays various HUD elements.
 * <p>
 * This feature manages HUD overlays including:
 * - PokeRod bait display above hotbar
 * - Spawn tracker (future feature)
 * </p>
 * <p>
 * HUD rendering is handled by classes in the hud package through
 * client-side rendering events.
 * </p>
 */
public class HudFeature implements Feature {
    
    @Override
    public void initialize() {
        // HUD overlays are registered via GuiMixin
        // No additional initialization required
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
}
