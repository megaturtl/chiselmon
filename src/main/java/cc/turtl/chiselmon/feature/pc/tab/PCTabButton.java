package cc.turtl.chiselmon.feature.pc.tab;

import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.TextRenderUtil;
import com.cobblemon.mod.common.CobblemonSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static cc.turtl.chiselmon.util.ComponentUtil.modResource;

public class PCTabButton extends Button {
    private static final ResourceLocation SPRITE = modResource("textures/gui/pc/pc_button_tab.png");

    private static final int TEXTURE_WIDTH = 35;
    public static final int BUTTON_WIDTH = TEXTURE_WIDTH;
    private static final int TEXTURE_HEIGHT = 20;
    public static final int BUTTON_HEIGHT = (int) (TEXTURE_HEIGHT / 2.0F);

    private static final int HOVERED_Y_OFFSET = BUTTON_HEIGHT;

    private final Tooltip tooltip;

    public PCTabButton(int x, int y, int forBox, Component boxName, OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, boxName, onPress, DEFAULT_NARRATION);
        this.tooltip = Tooltip
                .create(ComponentUtil.modTranslatable("pc.tab_button.tooltip", this.getMessage(),
                        (forBox + 1)));
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
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }
}