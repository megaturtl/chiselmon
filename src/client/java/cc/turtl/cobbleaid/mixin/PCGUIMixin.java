package cc.turtl.cobbleaid.mixin;

import cc.turtl.cobbleaid.features.pc.PcSort;
import cc.turtl.cobbleaid.features.pc.gui.PcButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    private static final int MENU_BUTTON_WIDTH = 40;
    private static final int MENU_BUTTON_HEIGHT = 14;
    private static final int SORT_BUTTON_WIDTH = 34;
    private static final int SORT_BUTTON_HEIGHT = 12;
    private static final int BUTTON_SPACING = 2;
    private static final int MENU_OFFSET_X = 352;
    private static final int MENU_OFFSET_Y = 10;

    private static final PcSort.SortType[][] SORT_TYPE_PAIRS = {
        {PcSort.SortType.SIZE_SMALLEST_TO_LARGEST, PcSort.SortType.SIZE_LARGEST_TO_SMALLEST},
        {PcSort.SortType.IV_TOTAL_LOW_TO_HIGH, PcSort.SortType.IV_TOTAL_HIGH_TO_LOW},
        {PcSort.SortType.POKEDEX_ASCENDING, PcSort.SortType.POKEDEX_DESCENDING},
        {PcSort.SortType.NAME_A_TO_Z, PcSort.SortType.NAME_Z_TO_A}
    };

    private static final String[][] BUTTON_LABELS = {
        {"Size ↑", "Size ↓"},
        {"IV ↑", "IV ↓"},
        {"Dex ↑", "Dex ↓"},
        {"A-Z", "Z-A"}
    };

    @Shadow(remap = false)
    @Final
    public static int BASE_WIDTH;

    @Shadow(remap = false)
    @Final
    public static int BASE_HEIGHT;

    @Shadow(remap = false)
    @Final
    private StorageWidget storageWidget;

    @Shadow(remap = false)
    public abstract ClientPC getPc();

    private PcButton cobbleaid$sortMenuButton;
    private final List<PcButton> cobbleaid$sortButtons = new ArrayList<>();

    protected PCGUIMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addSortButtons(CallbackInfo ci) {
        if (storageWidget == null) return;

        ClientPC pc = getPc();
        if (pc == null) return;

        int pcX = (width - BASE_WIDTH) / 2;
        int pcY = (height - BASE_HEIGHT) / 2;
        int menuButtonX = pcX + MENU_OFFSET_X;
        int menuButtonY = pcY + MENU_OFFSET_Y;

        createSortButtons(menuButtonX, menuButtonY, pc);
        createMenuButton(menuButtonX, menuButtonY);
    }

    private void createSortButtons(int menuX, int menuY, ClientPC pc) {
        Supplier<Integer> getCurrentBox = () -> storageWidget != null ? storageWidget.getBox() : -1;

        for (int row = 0; row < SORT_TYPE_PAIRS.length; row++) {
            for (int col = 0; col < 2; col++) {
                PcSort.SortType sortType = SORT_TYPE_PAIRS[row][col];
                String label = BUTTON_LABELS[row][col];

                int x = menuX + col * (SORT_BUTTON_WIDTH + BUTTON_SPACING);
                int y = menuY + MENU_BUTTON_HEIGHT + BUTTON_SPACING 
                        + row * (SORT_BUTTON_HEIGHT + BUTTON_SPACING);

                PcButton sortButton = new PcButton(
                    x, y, SORT_BUTTON_WIDTH, SORT_BUTTON_HEIGHT,
                    Component.literal(label),
                    btn -> sortCurrentBox(pc, getCurrentBox, sortType)
                );

                sortButton.visible = false;
                cobbleaid$sortButtons.add(sortButton);
                addRenderableWidget(sortButton);
            }
        }
    }

    private void createMenuButton(int x, int y) {
        cobbleaid$sortMenuButton = new PcButton(
            x, y, MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT,
            Component.literal("Sort ▼"),
            btn -> toggleSortDropdown()
        );

        cobbleaid$sortMenuButton.onPress();
        addRenderableWidget(cobbleaid$sortMenuButton);
    }

    private void sortCurrentBox(ClientPC pc, Supplier<Integer> getCurrentBox, PcSort.SortType sortType) {
        Integer currentBox = getCurrentBox.get();
        if (currentBox != null && currentBox >= 0) {
            PcSort.sortCurrentBox(pc, currentBox, sortType);
        }
    }

    private void toggleSortDropdown() {
        boolean expanded = cobbleaid$sortMenuButton.isToggled();
        cobbleaid$sortMenuButton.setMessage(Component.literal(expanded ? "Sort ▲" : "Sort ▼"));
        
        for (PcButton sortBtn : cobbleaid$sortButtons) {
            sortBtn.visible = expanded;
        }
    }
}