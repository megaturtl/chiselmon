package cc.turtl.chiselmon.feature.pc.bookmark;

import cc.turtl.chiselmon.feature.pc.PCButton;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.client.CobblemonResources;
import com.cobblemon.mod.common.client.storage.ClientBox;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Factory methods for creating bookmark-related buttons.
 * Separated from BookmarkManager to keep concerns clean.
 */
public class BookmarkButtons {

    /**
     * Create a bookmark toggle button.
     */
    public static PCButton createBookmarkButton(int x, int y, boolean isBookmarked, PCButton.OnPress onPress) {
        return new PCButton.Builder(
                x, y,
                BookmarkConstants.BOOKMARK_SPRITE,
                BookmarkConstants.BOOKMARK_TEXTURE_WIDTH,
                BookmarkConstants.BOOKMARK_TEXTURE_HEIGHT,
                onPress)
                .tooltips(BookmarkConstants.BOOKMARK_ACTIVE_TOOLTIP, BookmarkConstants.BOOKMARK_INACTIVE_TOOLTIP)
                .active(isBookmarked)
                .build();
    }

    /**
     * Create a home button.
     */
    public static PCButton createHomeButton(int x, int y, PCButton.OnPress onPress) {
        return new PCButton.Builder(
                x, y,
                BookmarkConstants.HOME_SPRITE,
                BookmarkConstants.HOME_TEXTURE_WIDTH,
                BookmarkConstants.HOME_TEXTURE_HEIGHT,
                onPress)
                .tooltip(BookmarkConstants.HOME_TOOLTIP)
                .build();
    }

    /**
     * Create a single tab button.
     */
    public static PCButton createTabButton(int x, int y, int boxNumber, Component boxName, PCButton.OnPress onPress) {
        Tooltip tooltip = Tooltip.create(
                Component.translatable("chiselmon.pc.tab_button.tooltip", boxName, boxNumber + 1));

        return new PCButton.Builder(
                x, y,
                BookmarkConstants.TAB_SPRITE,
                BookmarkConstants.TAB_TEXTURE_WIDTH,
                BookmarkConstants.TAB_TEXTURE_HEIGHT,
                onPress)
                .text(boxName)
                .textStyle(ColorUtils.WHITE.getRGB(), BookmarkConstants.TAB_TEXT_MARGIN)
                .tooltip(tooltip)
                .build();
    }

    /**
     * Create multiple tab buttons from a list of box numbers.
     */
    public static List<PCButton> createTabButtons(
            Collection<Integer> bookmarkedBoxes,
            List<ClientBox> clientBoxes,
            int startX,
            int startY,
            TabClickHandler onTabClick) {

        List<PCButton> buttons = new ArrayList<>();

        if (bookmarkedBoxes.isEmpty()) {
            return buttons;
        }

        int currentX = startX;
        int buttonWidth = BookmarkConstants.TAB_TEXTURE_WIDTH;

        for (Integer boxNumber : bookmarkedBoxes) {
            if (boxNumber >= clientBoxes.size() || boxNumber < 0) {
                continue;
            }

            Component boxName = formatBoxName(clientBoxes, boxNumber);

            PCButton button = createTabButton(
                    currentX,
                    startY,
                    boxNumber,
                    boxName,
                    (btn) -> onTabClick.onTabClick(boxNumber));

            buttons.add(button);
            currentX += buttonWidth + BookmarkManager.TAB_HORIZONTAL_SPACING;
        }

        return buttons;
    }

    /**
     * Format the display name for a box.
     */
    private static Component formatBoxName(List<ClientBox> clientBoxes, int boxNumber) {
        Component boxNameString = clientBoxes.get(boxNumber).getName();

        MutableComponent boxName = Component.empty();
        boxName.append(Objects.requireNonNullElseGet(boxNameString,
                () -> Component.translatable("cobblemon.ui.pc.box.title", boxNumber + 1)));

        return boxName
                .setStyle(boxName.getStyle().withFont(CobblemonResources.INSTANCE.getDEFAULT_LARGE()))
                .withStyle(ChatFormatting.BOLD);
    }

    /**
     * Functional interface for tab click handling.
     */
    @FunctionalInterface
    public interface TabClickHandler {
        void onTabClick(int boxNumber);
    }
}