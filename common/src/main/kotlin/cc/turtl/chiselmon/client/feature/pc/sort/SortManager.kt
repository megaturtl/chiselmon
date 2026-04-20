package cc.turtl.chiselmon.client.feature.pc.sort

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode
import com.cobblemon.mod.common.client.gui.pc.IconButton
import com.cobblemon.mod.common.client.gui.pc.StorageWidget
import com.cobblemon.mod.common.client.storage.ClientPC
import net.minecraft.client.gui.screens.Screen
import java.util.function.Consumer

class SortManager(
    private val pc: ClientPC,
    private val storage: StorageWidget,
    private val displayOptions: Boolean,
    private val optionButtons: MutableList<IconButton>,
    private val widgetAdder: Consumer<IconButton>
) {
    fun initialize(x: Int, y: Int) {
        val vanillaCount = PokemonSortMode.entries.size
        var btnX = x + 92 + (12 * vanillaCount)
        val btnY = y + 31

        SortMode.entries
            .filter { it.showInUI }
            .forEach { mode ->
                IconButton(
                    btnX, btnY, 20, 20,
                    mode.icon, mode.iconReversed,
                    mode.tooltipKey, mode.labelKey
                ) { BoxSorter.sortPCBox(pc, storage.box, mode, Screen.hasShiftDown()) }.also {
                    it.visible = displayOptions
                    widgetAdder.accept(it)
                    optionButtons.add(it)
                }
                btnX += 12
            }
    }

    fun executeQuickSort(mode: SortMode, reversed: Boolean) {
        storage.resetSelected()
        BoxSorter.sortPCBox(pc, storage.box, mode, reversed)
    }
}