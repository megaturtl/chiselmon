package cc.turtl.cobbleaid.feature.pc.tab;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.CobblemonRenderable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PCBookmarkButton extends Button implements CobblemonRenderable {
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("cobbleaid",
            "textures/gui/pc/pc_button_bookmark.png");

    private static final int TEXTURE_WIDTH = 15;
    private static final int TEXTURE_HEIGHT = 28;

    public static final int BUTTON_WIDTH = TEXTURE_WIDTH;
    public static final int BUTTON_HEIGHT = (int) (TEXTURE_HEIGHT / 2.0F);

    private static final int HOVERED_Y_OFFSET = BUTTON_HEIGHT;

    public PCBookmarkButton(int x, int y, OnPress onPress) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int pMouseX, int pMouseY, float pPartialTicks) {
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
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        soundManager.play((SoundInstance) SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }
}