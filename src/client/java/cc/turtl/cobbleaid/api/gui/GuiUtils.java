package cc.turtl.cobbleaid.api.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiUtils {

    public static void drawButtonBackground(GuiGraphics context, int x, int y, int width, int height,
            int borderColor, int fillColor, boolean hasBorder) {
        if (hasBorder) {
            context.fill(x, y, x + width, y + height, borderColor);
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, fillColor);
        } else {
            context.fill(x, y, x + width, y + height, fillColor);
        }
    }

    public static void drawScaledText(GuiGraphics context, Component text, int x, int y,
            int maxWidth, int maxHeight, int color, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        // Calculate scale to fit bounds (with padding of 4 pixels on each side)
        float horizontalScale = (maxWidth - 4f) / textWidth;
        float verticalScale = (maxHeight - 4f) / textHeight;
        float scale = Math.min(Math.min(horizontalScale, verticalScale), 1.0f);

        // Calculate centered position
        float scaledWidth = textWidth * scale;
        float scaledHeight = textHeight * scale;

        float centerX = x + maxWidth / 2f;
        float centerY = y + maxHeight / 2f;

        float renderX = centerX - scaledWidth / 2f;
        float renderY = centerY - scaledHeight / 2f;

        PoseStack poseStack = context.pose();
        poseStack.pushPose();

        // Translate to the calculated render position
        poseStack.translate(renderX, renderY, 0);

        // Apply the determined scale
        poseStack.scale(scale, scale, 1.0f);

        // The text is drawn at (0, 0) relative to the translated position
        context.drawString(font, text, 0, 0, color, shadow);

        poseStack.popPose();
    }
}