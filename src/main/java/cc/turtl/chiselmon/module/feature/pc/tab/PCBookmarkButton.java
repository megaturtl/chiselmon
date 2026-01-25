package cc.turtl.chiselmon.module.feature.pc.tab;

import static cc.turtl.chiselmon.util.ComponentUtil.modResource;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.CobblemonRenderable;

import cc.turtl.chiselmon.util.ComponentUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PCBookmarkButton extends Button implements CobblemonRenderable {
    private static final ResourceLocation SPRITE = modResource("textures/gui/pc/pc_button_bookmark.png");

    private static final int TEXTURE_WIDTH = 15;
    private static final int TEXTURE_HEIGHT = 30;

    public static final int BUTTON_WIDTH = TEXTURE_WIDTH;
    public static final int BUTTON_HEIGHT = (int) (TEXTURE_HEIGHT / 2.0F);

    private static final Tooltip TOOLTIP_ON = Tooltip.create(ComponentUtil.modTranslatable("pc.bookmark_button.tooltip.remove"));
    private static final Tooltip TOOLTIP_OFF = Tooltip.create(ComponentUtil.modTranslatable("pc.bookmark_button.tooltip.add"));

    public boolean toggled = false;

    public PCBookmarkButton(int x, int y, OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTicks) {

        if (this.isToggled()) {
            this.setTooltip(TOOLTIP_ON);
        } else {
            this.setTooltip(TOOLTIP_OFF);
        }

        // invert the offset if toggled
        int textureYOffset = this.isToggled()
                ? (this.isHovered() ? 0 : BUTTON_HEIGHT)
                : (this.isHovered() ? BUTTON_HEIGHT : 0);

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
        soundManager.play((SoundInstance) SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}