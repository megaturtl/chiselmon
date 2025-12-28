package cc.turtl.cobbleaid.feature.pc.tab;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.CobblemonSounds;

import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.TextRenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import static cc.turtl.cobbleaid.util.TextUtil.modResource;

public class PCTabButton extends Button {
    private static final ResourceLocation SPRITE = modResource("textures/gui/pc/pc_button_tab.png");

    private static final int TEXTURE_WIDTH = 35;
    private static final int TEXTURE_HEIGHT = 20;

    public static final int BUTTON_WIDTH = TEXTURE_WIDTH;
    public static final int BUTTON_HEIGHT = (int) (TEXTURE_HEIGHT / 2.0F);

    private static final int HOVERED_Y_OFFSET = BUTTON_HEIGHT;

    private final int forBox;

    private final Tooltip tooltip;

    public PCTabButton(int x, int y, int forBox, Component boxName, OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, boxName, onPress, DEFAULT_NARRATION);
        this.forBox = forBox;
        this.tooltip = Tooltip
                .create(Component.literal("Jump to " + this.getMessage().getString() + " (pg. " + (this.forBox + 1) + ")"));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.setTooltip(tooltip);
        int textureYOffset = this.isHovered() ? HOVERED_Y_OFFSET : 0;

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

        int centerX = this.getX() + BUTTON_WIDTH / 2;
        int centerY = this.getY() + BUTTON_HEIGHT / 2;

        TextRenderUtil.renderCenteredText(
                context,
                this.getMessage(),
                ColorUtil.WHITE,
                centerX,
                centerY,
                BUTTON_WIDTH - 5);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        soundManager.play((SoundInstance) SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }

    public int getBox() {
        return this.forBox;
    }
}