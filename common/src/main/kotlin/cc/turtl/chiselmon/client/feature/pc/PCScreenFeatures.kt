package cc.turtl.chiselmon.client.feature.pc

import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.feature.pc.bookmark.BookmarkManager
import cc.turtl.chiselmon.client.feature.pc.sort.SortManager
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.turtlshell.api.client.keybind.KeybindHelper
import com.cobblemon.mod.common.client.gui.pc.IconButton
import com.cobblemon.mod.common.client.gui.pc.StorageWidget
import com.cobblemon.mod.common.client.storage.ClientPC
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import java.util.function.Consumer

class PCScreenFeatures(
    private val pc: ClientPC,
    private val storageWidget: StorageWidget,
    private val optionButtons: MutableList<IconButton>,
    private val guiLeft: Int,
    private val guiTop: Int,
    private val widgetAdder: Consumer<AbstractWidget>,
    private val widgetRemover: Consumer<AbstractWidget>,
) {
    private var bookmarkManager: BookmarkManager? = null
    private var sortManager: SortManager? = null

    fun initialize(
        disabled: Boolean,
        displayOptions: Boolean,
    ) {
        if (disabled || bookmarkManager != null) return
        val worldScope = Scope.currentWorld() ?: return

        bookmarkManager =
            BookmarkManager(
                ChiselmonStorage.PC_SETTINGS.get(worldScope).bookmarks,
                storageWidget,
                pc,
                widgetAdder,
                widgetRemover,
            ).also { it.initialize(guiLeft, guiTop) }
        sortManager =
            SortManager(
                pc,
                storageWidget,
                displayOptions,
                optionButtons,
                widgetAdder,
                widgetRemover,
            ).also { it.initialize(guiLeft, guiTop) }
    }

    fun update(
        disabled: Boolean,
        displayOptions: Boolean,
    ) {
        if (disabled) {
            cleanup()
            return
        }
        initialize(false, displayOptions)
        bookmarkManager?.update()
        val sorter = sortManager ?: return

        val quickSort = ChiselmonConfig.pc.quickSort
        if (quickSort.enabled && KeybindHelper.isDown(quickSort.hotkey)) {
            sorter.executeQuickSort(quickSort.mode, Screen.hasShiftDown())
        }
    }

    fun cleanup() {
        bookmarkManager?.cleanup()
        bookmarkManager = null
        sortManager?.cleanup()
        sortManager = null
    }

    fun removed() {
        cleanup()
        Scope.currentWorld()?.let { ChiselmonStorage.PC_SETTINGS.save(it) }
    }
}
