package cc.turtl.cobbleaid.feature.pc.icons;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;

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
 * Icons are rendered by this feature and controlled via the
 * PC icons configuration section.
 * </p>
 */
public class PcIconsFeature implements Feature {
    
    private PcIconRenderer renderer;
    
    @Override
    public void initialize() {
        this.renderer = new PcIconRenderer();
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
    
    /**
     * Render icons for a Pokemon in the PC.
     * This is called from the mixin layer.
     * 
     * @param context the graphics context
     * @param pokemon the Pokemon to render icons for
     * @param posX the X position
     * @param posY the Y position
     */
    public void renderIcons(GuiGraphics context, Pokemon pokemon, int posX, int posY) {
        if (renderer != null && isEnabled()) {
            renderer.renderIconElements(context, pokemon, posX, posY);
        }
    }
}
