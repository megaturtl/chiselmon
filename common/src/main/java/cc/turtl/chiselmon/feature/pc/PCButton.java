package cc.turtl.chiselmon.feature.pc;

import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.render.TextRenderUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.CobblemonRenderable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Generic PC button with configurable sprite, tooltip, and optional text rendering.
 * All PC buttons should use this class directly rather than subclassing.
 */
public class PCButton extends Button implements CobblemonRenderable {

    private final ResourceLocation sprite;
    private final int textureWidth;
    private final int textureHeight;
    private final int buttonHeight;

    // Optional text rendering
    private final boolean renderText;
    private final int textColor;
    private final int textMargin;

    // Tooltip configuration
    private final Tooltip activeTooltip;
    private final Tooltip inactiveTooltip;

    // State
    private boolean active;

    private PCButton(Builder builder) {
        super(
                builder.x,
                builder.y,
                builder.width,
                builder.height,
                builder.text != null ? builder.text : Component.empty(),
                builder.onPress,
                DEFAULT_NARRATION
        );

        this.sprite = builder.sprite;
        this.textureWidth = builder.textureWidth;
        this.textureHeight = builder.textureHeight;
        this.buttonHeight = builder.height;

        this.renderText = builder.text != null;
        this.textColor = builder.textColor;
        this.textMargin = builder.textMargin;

        this.activeTooltip = builder.activeTooltip;
        this.inactiveTooltip = builder.inactiveTooltip;

        this.active = builder.initialActive;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        // Update tooltip based on state
        updateTooltip();

        // Calculate texture offset based on active state and hover
        int textureYOffset = calculateTextureYOffset();

        // Render the button sprite
        context.blit(
                sprite,
                getX(),
                getY(),
                0,
                textureYOffset,
                width,
                height,
                textureWidth,
                textureHeight
        );

        // Render text if configured
        if (renderText) {
            renderButtonText(context);
        }
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        soundManager.play((SoundInstance) SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F));
    }

    private int calculateTextureYOffset() {
        // When active: show bottom half when not hovered, top half when hovered
        // When inactive: show top half when not hovered, bottom half when hovered
        return active
                ? (isHovered() ? 0 : buttonHeight)
                : (isHovered() ? buttonHeight : 0);
    }

    private void updateTooltip() {
        if (activeTooltip != null && inactiveTooltip != null) {
            setTooltip(active ? activeTooltip : inactiveTooltip);
        } else if (inactiveTooltip != null) {
            setTooltip(inactiveTooltip);
        }
    }

    private void renderButtonText(GuiGraphics context) {
        int centerX = getX() + width / 2;
        int centerY = getY() + height / 2;

        TextRenderUtils.renderCenteredText(
                context,
                getMessage(),
                textColor,
                centerX,
                centerY,
                width - textMargin
        );
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    // Builder pattern for clean construction
    public static class Builder {
        // Required parameters
        private final int x;
        private final int y;
        private final ResourceLocation sprite;
        private final int textureWidth;
        private final int textureHeight;
        private final OnPress onPress;

        // Derived from texture
        private int width;
        private int height;

        // Optional parameters with defaults
        private Component text = null;
        private int textColor = ColorUtils.WHITE;
        private int textMargin = 5;
        private Tooltip activeTooltip = null;
        private Tooltip inactiveTooltip = null;
        private boolean initialActive = false;

        /**
         * Create a button builder.
         *
         * @param x X position
         * @param y Y position
         * @param sprite Texture location
         * @param textureWidth Full texture width
         * @param textureHeight Full texture height (should be 2x button height for hover states)
         * @param onPress Click handler
         */
        public Builder(int x, int y, ResourceLocation sprite, int textureWidth, int textureHeight, OnPress onPress) {
            this.x = x;
            this.y = y;
            this.sprite = sprite;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.onPress = onPress;

            // By default, button dimensions match texture (height is half for top/bottom states)
            this.width = textureWidth;
            this.height = textureHeight / 2;
        }

        /**
         * Override the button dimensions (useful if texture has padding).
         */
        public Builder dimensions(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Add text to be rendered on the button.
         */
        public Builder text(Component text) {
            this.text = text;
            return this;
        }

        /**
         * Configure text rendering appearance.
         */
        public Builder textStyle(int color, int margin) {
            this.textColor = color;
            this.textMargin = margin;
            return this;
        }

        /**
         * Set a single tooltip (shown regardless of active state).
         */
        public Builder tooltip(Tooltip tooltip) {
            this.inactiveTooltip = tooltip;
            return this;
        }

        /**
         * Set different tooltips for active and inactive states.
         * Useful for toggle buttons (e.g., "Add bookmark" vs "Remove bookmark").
         */
        public Builder tooltips(Tooltip activeTooltip, Tooltip inactiveTooltip) {
            this.activeTooltip = activeTooltip;
            this.inactiveTooltip = inactiveTooltip;
            return this;
        }

        /**
         * Set the initial active state (default: false).
         */
        public Builder active(boolean active) {
            this.initialActive = active;
            return this;
        }

        public PCButton build() {
            return new PCButton(this);
        }
    }
}