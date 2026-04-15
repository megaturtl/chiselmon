package cc.turtl.chiselmon.client.feature.pc.bookmark

import cc.turtl.chiselmon.feature.pc.PCButton
import cc.turtl.turtlshell.api.core.format.ColorLib
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.storage.ClientBox
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component

internal object BookmarkButtons {

    fun createBookmarkButton(x: Int, y: Int, isBookmarked: Boolean, onPress: () -> Unit): PCButton =
        PCButton.Builder(
            x,
            y,
            BookmarkConstants.BOOKMARK_SPRITE,
            BookmarkConstants.BOOKMARK_TEXTURE_WIDTH,
            BookmarkConstants.BOOKMARK_TEXTURE_HEIGHT
        ) { onPress() }
            .tooltips(BookmarkConstants.BOOKMARK_ACTIVE_TOOLTIP, BookmarkConstants.BOOKMARK_INACTIVE_TOOLTIP)
            .active(isBookmarked)
            .build()

    fun createHomeButton(x: Int, y: Int, onPress: () -> Unit): PCButton =
        PCButton.Builder(
            x,
            y,
            BookmarkConstants.HOME_SPRITE,
            BookmarkConstants.HOME_TEXTURE_WIDTH,
            BookmarkConstants.HOME_TEXTURE_HEIGHT
        ) { onPress() }
            .tooltip(BookmarkConstants.HOME_TOOLTIP)
            .build()

    fun createTabButton(x: Int, y: Int, boxNumber: Int, boxName: Component, onPress: () -> Unit): PCButton =
        PCButton.Builder(
            x,
            y,
            BookmarkConstants.TAB_SPRITE,
            BookmarkConstants.TAB_TEXTURE_WIDTH,
            BookmarkConstants.TAB_TEXTURE_HEIGHT
        ) { onPress() }
            .text(boxName)
            .textStyle(ColorLib.WHITE.rgb, BookmarkConstants.TAB_TEXT_MARGIN)
            .tooltip(Tooltip.create(Component.translatable("chiselmon.pc.tab_button.tooltip", boxName, boxNumber + 1)))
            .build()

    fun createTabButtons(
        bookmarkedBoxes: Collection<Int>,
        clientBoxes: List<ClientBox>,
        startX: Int,
        startY: Int,
        onTabClick: (Int) -> Unit
    ): List<PCButton> {
        if (bookmarkedBoxes.isEmpty()) return emptyList()

        var currentX = startX
        return bookmarkedBoxes.mapNotNull { boxNumber ->
            if (boxNumber !in clientBoxes.indices) return@mapNotNull null

            val boxName = formatBoxName(clientBoxes, boxNumber)
            createTabButton(currentX, startY, boxNumber, boxName) { onTabClick(boxNumber) }
                .also { currentX += BookmarkConstants.TAB_TEXTURE_WIDTH + BookmarkConstants.TAB_HORIZONTAL_SPACING }
        }
    }

    private fun formatBoxName(clientBoxes: List<ClientBox>, boxNumber: Int): Component {
        val name = clientBoxes[boxNumber].name
            ?: Component.translatable("cobblemon.ui.pc.box.title", boxNumber + 1)
        return Component.empty()
            .append(name)
            .setStyle(name.style.withFont(CobblemonResources.DEFAULT_LARGE))
            .withStyle(ChatFormatting.BOLD)
    }
}