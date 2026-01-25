package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.module.feature.hud.PokeRodBaitOverlay;
import cc.turtl.chiselmon.module.ChiselmonModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class HudModule implements ChiselmonModule {
    public static final String ID = "hud";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void initialize() {
    }

    public boolean shouldRenderPokeRodOverlay(ItemStack itemStack) {
        return PokeRodBaitOverlay.shouldRender(itemStack);
    }

    public void renderPokeRodOverlay(GuiGraphics guiGraphics) {
        PokeRodBaitOverlay.renderPokeRodOverlay(guiGraphics);
    }
}
