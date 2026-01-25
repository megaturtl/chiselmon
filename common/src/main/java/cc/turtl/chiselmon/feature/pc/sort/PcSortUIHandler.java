package cc.turtl.chiselmon.feature.pc.sort;

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class PcSortUIHandler {

    // Helper interface to allow the handler to add buttons to the PCGUI
    public interface ButtonAdder {
        void addRenderableWidget(IconButton button);

        boolean isDisplayingOptions();

        List<IconButton> getOptionButtons();
    }

    private PcSortUIHandler() {
    }

    public static void initializeSortButtons(
            PCGUI screen,
            ClientPC pc,
            StorageWidget storage,
            ButtonAdder adder,
            int screenWidth,
            int screenHeight,
            int baseWidth,
            int baseHeight) {

        int x = (screenWidth - baseWidth) / 2;
        int y = (screenHeight - baseHeight) / 2;

        // Start position calculation
        int existingCount = PokemonSortMode.values().length;
        int btnX = x + 92 + (12 * existingCount);
        int btnY = y + 31;
        int btnWidth = 20;
        int btnHeight = 20;

        for (PokemonCustomSortMode sortType : PokemonCustomSortMode.values()) {

            if (!sortType.showInUI()) {
                continue;
            }

            IconButton customSortBtn = new IconButton(
                    btnX, btnY,
                    btnWidth, btnHeight,
                    sortType.icon(),
                    sortType.iconReversed(),
                    sortType.tooltipKey(),
                    sortType.labelKey(),
                    btn -> PcSorter.sortPCBox(
                            pc,
                            storage.getBox(),
                            sortType,
                            Screen.hasShiftDown()));

            customSortBtn.visible = adder.isDisplayingOptions();
            adder.addRenderableWidget(customSortBtn);
            adder.getOptionButtons().add(customSortBtn);

            btnX += 12;
        }
    }
}