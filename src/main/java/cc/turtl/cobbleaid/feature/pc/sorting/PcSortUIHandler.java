package cc.turtl.cobbleaid.feature.pc.sorting;

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PcSortUIHandler {

    // Helper interface to allow the handler to add buttons to the PCGUI
    public interface ButtonAdder {
        void addRenderableWidget(IconButton button);
        boolean isDisplayingOptions();
        List<IconButton> getOptionButtons();
    }

    private PcSortUIHandler() {
        // Prevent instantiation
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

        for (PcSorter.SortType sortType : PcSorter.SortType.values()) {
            String typeName = sortType.toString().toLowerCase();
            String tooltipKey = "ui.sort." + typeName;
            String labelKey = "sort_" + typeName;

            ResourceLocation btnTexture = ResourceLocation.fromNamespaceAndPath(
                    "cobbleaid",
                    "textures/gui/pc/pc_button_sort_" + typeName + ".png");
            ResourceLocation btnTextureAlt = ResourceLocation.fromNamespaceAndPath(
                    "cobbleaid",
                    "textures/gui/pc/pc_button_sort_" + typeName + "_reverse.png");

            IconButton customSortBtn = new IconButton(
                    btnX, btnY,
                    btnWidth, btnHeight,
                    btnTexture,
                    btnTextureAlt,
                    tooltipKey,
                    labelKey,
                    btn -> PcSorter.sortPCBox(pc, storage.getBox(), sortType, Screen.hasShiftDown()));

            // Sync with the options button visibility
            customSortBtn.visible = adder.isDisplayingOptions();

            // Add to screen and internal list via the adder interface
            adder.addRenderableWidget(customSortBtn);
            adder.getOptionButtons().add(customSortBtn);

            btnX += 12;
        }
    }
}