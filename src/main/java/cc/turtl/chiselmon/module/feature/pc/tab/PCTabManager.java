package cc.turtl.chiselmon.module.feature.pc.tab;

import java.util.ArrayList;
import java.util.List;

import com.cobblemon.mod.common.client.CobblemonResources;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientBox;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PCTabManager {
    public static final int HORIZONTAL_SPACING = 2;

    public static List<PCTabButton> createTabButtons(StorageWidget storageWidget, List<PCTab> tabs,
            int startX,
            int startY) {
        List<PCTabButton> buttons = new ArrayList<>();

        if (tabs.isEmpty())
            return buttons;

        int currentX = startX;

        final List<ClientBox> clientBoxes = storageWidget.getPcGui().getPc().getBoxes();

        for (PCTab tab : tabs) {
            final int targetBoxNumber = tab.boxNumber;

            if (targetBoxNumber >= clientBoxes.size()) {
                continue;
            }

            Button.OnPress onPress = (button) -> {
                storageWidget.setBox(targetBoxNumber);
            };

            Component boxNameString = clientBoxes.get(targetBoxNumber).getName();

            MutableComponent boxName = Component.empty();
            if (boxNameString == null)
                boxName.append(Component.translatable("cobblemon.ui.pc.box.title", targetBoxNumber + 1));
            else {
                boxName.append(boxNameString);
            }
            boxName.setStyle(boxName.getStyle().withFont(CobblemonResources.INSTANCE.getDEFAULT_LARGE()))
                    .withStyle(ChatFormatting.BOLD);

            PCTabButton button = new PCTabButton(
                    currentX,
                    startY,
                    targetBoxNumber,
                    boxName,
                    onPress);
            buttons.add(button);

            currentX += PCTabButton.BUTTON_WIDTH + HORIZONTAL_SPACING;
        }

        return buttons;
    }
}
