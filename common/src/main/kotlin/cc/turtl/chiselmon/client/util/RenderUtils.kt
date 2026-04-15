package cc.turtl.chiselmon.client.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/**
 * Renders text centered horizontally within a width constraint, using
 * Minecraft's built-in text trimming if needed. Vertical centering is also handled.
 */
fun renderCenteredText(
    graphics: GuiGraphics,
    text: Component,
    color: Int,
    centerX: Int,
    centerY: Int,
    maxWidth: Int
) {
    val font = Minecraft.getInstance().font

    // Let Minecraft handle the text trimming
    var displayText = text
    var textWidth = font.width(text)

    if (textWidth > maxWidth) {
        displayText = Component.literal(font.plainSubstrByWidth(text.string, maxWidth).trim { it <= ' ' })
            .withStyle(text.style)
        textWidth = font.width(displayText)
    }

    // Calculate position for centered text
    val x = centerX - textWidth / 2
    val y = centerY - font.lineHeight / 2

    graphics.drawString(font, displayText, x, y, color)
}