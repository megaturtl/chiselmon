package cc.turtl.chiselmon.client.feature.pc.sort

import cc.turtl.chiselmon.client.config.ChiselmonConfig
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
    private val widgetAdder: Consumer<in IconButton>,
    private val widgetRemover: Consumer<in IconButton>,
) {
    private val buttons = mutableListOf<IconButton>()

    fun initialize(
        x: Int,
        y: Int,
    ) {
        val vanillaCount = PokemonSortMode.entries.size
        var btnX = x + 92 + (12 * vanillaCount)
        val btnY = y + 31

        SortMode.entries
            .filter { it.showInUI }
            .forEach { mode ->
                IconButton(
                    btnX,
                    btnY,
                    20,
                    20,
                    mode.icon,
                    mode.iconReversed,
                    mode.tooltipKey,
                    mode.labelKey,
                ) { executeQuickSort(mode, Screen.hasShiftDown()) }.also {
                    it.visible = displayOptions
                    widgetAdder.accept(it)
                    optionButtons.add(it)
                    buttons.add(it)
                }
                btnX += 12
            }
    }

    fun cleanup() {
        buttons.forEach {
            optionButtons.remove(it)
            widgetRemover.accept(it)
        }
        buttons.clear()
    }

    fun executeQuickSort(
        mode: SortMode,
        reversed: Boolean,
    ) {
        if (ChiselmonConfig.general.modDisabled) return
        storage.resetSelected()
        BoxSorter.sortPCBox(pc, storage.box, mode, reversed)
    }
}
