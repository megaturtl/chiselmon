package cc.turtl.chiselmon.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextRenderUtil {
    /**
     * Renders text centered horizontally within a width constraint, using
     * Minecraft's
     * built-in text trimming if needed. Vertical centering is also handled.
     *
     * @param graphics GuiGraphics context
     * @param text     The text component to render
     * @param color    Text color
     * @param centerX  The X-coordinate of the center
     * @param centerY  The Y-coordinate of the center
     * @param maxWidth Maximum width for the text
     */
    public static void renderCenteredText(
            GuiGraphics graphics,
            Component text,
            int color,
            int centerX,
            int centerY,
            int maxWidth) {

        Font font = Minecraft.getInstance().font;

        // Let Minecraft handle the text trimming
        Component displayText = text;
        int textWidth = font.width(text);

        if (textWidth > maxWidth) {
            displayText = Component.literal(font.plainSubstrByWidth(text.getString(), maxWidth).trim())
                    .withStyle(displayText.getStyle());
            textWidth = font.width(displayText);
        }

        // Calculate position for centered text
        int x = centerX - textWidth / 2;
        int y = centerY - font.lineHeight / 2;

        graphics.drawString(font, displayText, x, y, color);
    }
}