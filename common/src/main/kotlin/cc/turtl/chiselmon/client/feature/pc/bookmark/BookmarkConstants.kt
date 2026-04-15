package cc.turtl.chiselmon.client.feature.pc.bookmark

import cc.turtl.chiselmon.core.util.modResource
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component

internal object BookmarkConstants {
    // Layout
    const val BOOKMARK_BUTTON_OFFSET_X = 239
    const val BOOKMARK_BUTTON_OFFSET_Y = 12
    const val HOME_BUTTON_OFFSET_X = 90
    const val HOME_BUTTON_OFFSET_Y = 12
    const val TAB_START_OFFSET_X = 80
    const val TAB_START_OFFSET_Y = -5
    const val TAB_HORIZONTAL_SPACING = 2

    // Bookmark button
    val BOOKMARK_SPRITE = modResource("textures/gui/pc/pc_button_bookmark.png")
    const val BOOKMARK_TEXTURE_WIDTH = 15
    const val BOOKMARK_TEXTURE_HEIGHT = 30
    val BOOKMARK_ACTIVE_TOOLTIP: Tooltip =
        Tooltip.create(Component.translatable("chiselmon.pc.bookmark_button.tooltip.remove"))
    val BOOKMARK_INACTIVE_TOOLTIP: Tooltip =
        Tooltip.create(Component.translatable("chiselmon.pc.bookmark_button.tooltip.add"))

    // Home button
    val HOME_SPRITE = modResource("textures/gui/pc/pc_button_home.png")
    const val HOME_TEXTURE_WIDTH = 15
    const val HOME_TEXTURE_HEIGHT = 30
    val HOME_TOOLTIP: Tooltip = Tooltip.create(Component.translatable("chiselmon.pc.home_button.tooltip"))

    // Tab button
    val TAB_SPRITE = modResource("textures/gui/pc/pc_button_tab.png")
    const val TAB_TEXTURE_WIDTH = 35
    const val TAB_TEXTURE_HEIGHT = 20
    const val TAB_TEXT_MARGIN = 5
}