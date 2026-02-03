package cc.turtl.chiselmon.feature.pc.sort;

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;
import java.util.function.Consumer;

public class SortManager {
    private final ClientPC pc;
    private final StorageWidget storage;
    private final boolean displayOptions;
    private final Consumer<IconButton> widgetAdder;
    private final List<IconButton> optionButtons;

    public SortManager(ClientPC pc, StorageWidget storage, boolean displayOptions, List<IconButton> optionButtons, Consumer<IconButton> widgetAdder) {
        this.pc = pc;
        this.storage = storage;
        this.displayOptions = displayOptions;
        this.optionButtons = optionButtons;
        this.widgetAdder = widgetAdder;
    }

    public void initialize(int x, int y) {
        // Count vanilla Cobblemon buttons to calculate offset for custom buttons
        int vanillaCount = PokemonSortMode.values().length;
        int btnX = x + 92 + (12 * vanillaCount);
        int btnY = y + 31;

        for (SortMode mode : SortMode.values()) {
            if (!mode.showInUI()) continue;

            IconButton btn = new IconButton(
                    btnX, btnY, 20, 20,
                    mode.icon(), mode.iconReversed(),
                    mode.tooltipKey(), mode.labelKey(),
                    click -> Sorter.sortPCBox(pc, storage.getBox(), mode, Screen.hasShiftDown())
            );

            btn.visible = displayOptions;

            // Important: Renders to the Screen and adds to Cobblemon's list of option buttons
            widgetAdder.accept(btn);
            optionButtons.add(btn);

            btnX += 12; // Spacing (consistent with Cobblemon)
        }
    }

    public void handleMiddleClick(int button) {
        if (button == 2 && ChiselmonConstants.CONFIG.pc.quickSortEnabled) {
            this.storage.resetSelected();
            Sorter.sortPCBox(this.pc, this.storage.getBox(),
                    ChiselmonConstants.CONFIG.pc.quickSortMode, Screen.hasShiftDown());
        }
    }
}