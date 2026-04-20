package cc.turtl.chiselmon.client.feature.pc.bookmark

import cc.turtl.chiselmon.client.feature.pc.PCButton
import cc.turtl.chiselmon.client.feature.pc.PCUserData
import com.cobblemon.mod.common.client.gui.pc.StorageWidget
import com.cobblemon.mod.common.client.storage.ClientPC
import java.util.function.Consumer

class BookmarkManager(
    private val bookmarkStore: PCUserData.Bookmarks,
    private val storageWidget: StorageWidget,
    private val pc: ClientPC,
    private val widgetAdder: Consumer<PCButton>,
    private val widgetRemover: Consumer<PCButton>
) {

    private val tabButtons = mutableListOf<PCButton>()
    private var bookmarkButton: PCButton? = null
    private var homeButton: PCButton? = null

    fun initialize(guiLeft: Int, guiTop: Int) {
        createBookmarkButton(guiLeft, guiTop)
        createHomeButton(guiLeft, guiTop)
        rebuildTabButtons(guiLeft, guiTop)
    }

    fun update() {
        bookmarkButton?.isActive = bookmarkStore.has(storageWidget.box)
    }

    fun cleanup() = removeAllButtons()

    private fun createBookmarkButton(guiLeft: Int, guiTop: Int) {
        bookmarkButton = BookmarkButtons.createBookmarkButton(
            guiLeft + BookmarkConstants.BOOKMARK_BUTTON_OFFSET_X,
            guiTop + BookmarkConstants.BOOKMARK_BUTTON_OFFSET_Y,
            bookmarkStore.has(storageWidget.box)
        ) { handleBookmarkClick(guiLeft, guiTop) }
            .also { widgetAdder.accept(it) }
    }

    private fun createHomeButton(guiLeft: Int, guiTop: Int) {
        homeButton = BookmarkButtons.createHomeButton(
            guiLeft + BookmarkConstants.HOME_BUTTON_OFFSET_X,
            guiTop + BookmarkConstants.HOME_BUTTON_OFFSET_Y
        ) { storageWidget.box = 0 }
            .also { widgetAdder.accept(it) }
    }

    private fun rebuildTabButtons(guiLeft: Int, guiTop: Int) {
        tabButtons.forEach { widgetRemover.accept(it) }
        tabButtons.clear()

        BookmarkButtons.createTabButtons(
            bookmarkStore.get(),
            pc.boxes,
            guiLeft + BookmarkConstants.TAB_START_OFFSET_X,
            guiTop + BookmarkConstants.TAB_START_OFFSET_Y
        ) { storageWidget.box = it }.forEach {
            widgetAdder.accept(it)
            tabButtons.add(it)
        }
    }

    private fun handleBookmarkClick(guiLeft: Int, guiTop: Int) {
        bookmarkStore.toggle(storageWidget.box)
        bookmarkButton?.toggleActive()
        rebuildTabButtons(guiLeft, guiTop)
    }

    private fun removeAllButtons() {
        bookmarkButton?.let { widgetRemover.accept(it); bookmarkButton = null }
        homeButton?.let { widgetRemover.accept(it); homeButton = null }
        tabButtons.forEach { widgetRemover.accept(it) }
        tabButtons.clear()
    }
}