package cc.turtl.chiselmon.client.feature

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.turtlshell.api.core.util.getRatioColor
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.item.interactive.PokerodItem.Companion.getBaitStackOnRod
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack

object PokeRodOverlay {
    private const val ICON_SIZE = 16
    private const val ICON_TEXT_SPACING = 3

    @JvmStatic
    fun render(guiGraphics: GuiGraphics) {
        if (!ChiselmonConfig.general.pokerodOverlay) return

        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return
        val heldItem = player.mainHandItem

        if (heldItem.item !is PokerodItem) return

        renderBaitText(guiGraphics, minecraft, getBaitStackOnRod(heldItem))
    }

    private fun renderBaitText(guiGraphics: GuiGraphics, minecraft: Minecraft, baitStack: ItemStack) {
        val player = minecraft.player ?: return
        val font = minecraft.font

        val baitCount = baitStack.count

        var y = guiGraphics.guiHeight() - 59
        if (player.isCreative && minecraft.gameMode?.canHurtPlayer() == false) y += 14

        if (baitCount > 0) {
            val displayText = "${baitStack.hoverName.string} x$baitCount"
            val totalWidth = ICON_SIZE + ICON_TEXT_SPACING + font.width(displayText)
            val startX = (guiGraphics.guiWidth() - totalWidth) / 2

            guiGraphics.renderItem(baitStack, startX, y - 4)
            guiGraphics.drawString(
                font,
                displayText,
                startX + ICON_SIZE + ICON_TEXT_SPACING,
                y,
                getRatioColor(baitCount / 64.0f),
                true
            )
        } else {
            val noBaitText = "No bait!"
            val textX = (guiGraphics.guiWidth() - font.width(noBaitText)) / 2
            guiGraphics.drawString(font, noBaitText, textX, y, getRatioColor(0f), true)
        }
    }

}