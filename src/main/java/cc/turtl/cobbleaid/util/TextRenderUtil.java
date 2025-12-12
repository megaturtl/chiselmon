package cc.turtl.cobbleaid.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextRenderUtil {
    /**
     * Renders a Component text centered and scaled to fit within a bounding box.
     * The scale factor is calculated to ensure the text fits both width and height,
     * prioritizing fitting within the bounding box (maxWidth, maxHeight).
     *
     * @param graphics GuiGraphics context.
     * @param text
     * @param color
     * @param centerX The X-coordinate of the center of the bounding box.
     * @param centerY The Y-coordinate of the center of the bounding box.
     * @param maxWidth The maximum width the text should occupy.
     * @param maxHeight The maximum height the text should occupy.
     * @param padding A small pixel amount for padding inside the bounding box.
     */
    public static void renderScaledCenteredText(
            GuiGraphics graphics,
            Component text,
            int color,
            int centerX,
            int centerY,
            int maxWidth,
            int maxHeight) {
        
        Font fontRenderer = Minecraft.getInstance().font;
        int textWidth = fontRenderer.width(text);
        int defaultFontHeight = fontRenderer.lineHeight;

        // 1. Calculate available space
        int usableWidth = maxWidth;
        int usableHeight = maxHeight;

        // 2. Calculate optimal scaling factors
        
        // Scale needed to fit width
        float scaleX = (float) usableWidth / textWidth;

        // Scale needed to fit height (default Minecraft font height is 9)
        float scaleY = (float) usableHeight / defaultFontHeight;

        // 3. Determine the final scale (use the smaller scale to ensure it fits both)
        float finalScale = Math.min(1.0f, Math.min(scaleX, scaleY));
        
        // Safety check for empty or zero-width components
        if (textWidth == 0 || usableWidth <= 0 || usableHeight <= 0) {
            finalScale = 1.0f; // Default scale if calculations fail
        }

        // 4. Apply transformation
        PoseStack matrices = graphics.pose();
        matrices.pushPose();
        matrices.scale(finalScale, finalScale, finalScale);

        // 5. Calculate scaled coordinates
        
        // Calculate the center of the button, then divide by the scale factor
        int scaledCenterX = Math.round(centerX / finalScale);
        
        // Calculate the top Y position. We use centerY and subtract half of the 
        // *scaled* font height, then divide by scale.
        // Formula: (centerY - (defaultFontHeight * finalScale / 2)) / finalScale
        // Simplified: (centerY / finalScale) - (defaultFontHeight / 2)
        float yTopScreen = (float) centerY - (defaultFontHeight * finalScale / 2.0F);
        int scaledTopY = Math.round(yTopScreen / finalScale);

        // 6. Render the text (drawCenteredString takes the center X and top Y)
        graphics.drawCenteredString(
                fontRenderer,
                text,
                scaledCenterX,
                scaledTopY,
                color);

        // 7. Restore matrix
        matrices.popPose();
    }
}
