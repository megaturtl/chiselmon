package cc.turtl.cobbleaid.feature.pc.eggs;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareDummyPokemon;
import net.minecraft.client.gui.GuiGraphics;

/**
 * PC Egg Preview Feature - Shows egg preview in PC slots.
 * <p>
 * This feature integrates with Neo Daycare to display egg previews
 * in PC slots, allowing users to see what Pokemon will hatch from eggs
 * before they hatch.
 * </p>
 * <p>
 * The egg rendering is handled by this feature through {@link PcEggRenderer}.
 * </p>
 */
public class PcEggsFeature implements Feature {
    
    private PcEggRenderer renderer;
    
    @Override
    public void initialize() {
        this.renderer = new PcEggRenderer();
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
    
    /**
     * Render egg preview for a Neo Daycare dummy Pokemon.
     * This is called from the mixin layer.
     * 
     * @param context the graphics context
     * @param dummyPokemon the dummy Pokemon representing the egg
     * @param posX the X position
     * @param posY the Y position
     */
    public void renderEggPreview(GuiGraphics context, NeoDaycareDummyPokemon dummyPokemon, int posX, int posY) {
        if (renderer != null && isEnabled()) {
            renderer.renderEggPreviewElements(context, dummyPokemon, posX, posY);
        }
    }
}
