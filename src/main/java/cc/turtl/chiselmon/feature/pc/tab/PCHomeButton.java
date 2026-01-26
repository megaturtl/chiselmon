package cc.turtl.chiselmon.feature.pc.tab;

import cc.turtl.chiselmon.util.ComponentUtil;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.CobblemonRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static cc.turtl.chiselmon.util.ComponentUtil.modResource;

public class PCHomeButton extends Button implements CobblemonRenderable {
    private static final ResourceLocation SPRITE = modResource("textures/gui/pc/pc_button_home.png");

    private static final int TEXTURE_WIDTH = 15;
    public static final int BUTTON_WIDTH = TEXTURE_WIDTH;
    private static final int TEXTURE_HEIGHT = 30;
    public static final int BUTTON_HEIGHT = (int) (TEXTURE_HEIGHT / 2.0F);

    private static final Tooltip TOOLTIP = Tooltip.create(ComponentUtil.modTranslatable("pc.home_button.tooltip"));

    public PCHomeButton(int x, int y, OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.setTooltip(TOOLTIP);

        // invert the offset if toggled
        int textureYOffset = (this.isHovered() ? BUTTON_HEIGHT : 0);

        context.blit(
                SPRITE,
                this.getX(),
                this.getY(),
                0,
                textureYOffset,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }
}