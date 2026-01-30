package cc.turtl.chiselmon.feature.pc.bookmark;

import java.util.*;
import java.util.function.Consumer;

import cc.turtl.chiselmon.feature.pc.PCButton;
import cc.turtl.chiselmon.leveldata.LevelDataHelper;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Manages PC bookmark UI with clean widget lifecycle management.
 * Uses callbacks to delegate widget add/remove to the parent screen.
 */
public class BookmarkManager {

    private final StorageWidget storageWidget;
    private final ClientPC pc;
    private final BookmarkStore bookmarkStore;

    // Widget lifecycle callbacks - let the parent screen handle this
    private final Consumer<PCButton> addWidget;
    private final Consumer<PCButton> removeWidget;

    // UI state
    private final List<PCButton> tabButtons = new ArrayList<>();
    private PCButton bookmarkButton;
    private PCButton homeButton;

    // Layout constants
    private static final int BOOKMARK_BUTTON_OFFSET_X = 239;
    private static final int BOOKMARK_BUTTON_OFFSET_Y = 12;
    private static final int HOME_BUTTON_OFFSET_X = 90;
    private static final int HOME_BUTTON_OFFSET_Y = 12;
    private static final int TAB_START_OFFSET_X = 80;
    private static final int TAB_START_OFFSET_Y = -5;
    public static final int TAB_HORIZONTAL_SPACING = 2;

    /**
     * Create a BookmarkManager with widget lifecycle callbacks.
     *
     * @param level The client level
     * @param storageWidget The PC storage widget
     * @param pc The client PC
     * @param addWidget Callback to add a widget to the screen
     * @param removeWidget Callback to remove a widget from the screen
     */
    public BookmarkManager(
            ClientLevel level,
            StorageWidget storageWidget,
            ClientPC pc,
            Consumer<PCButton> addWidget,
            Consumer<PCButton> removeWidget) {
        this.storageWidget = storageWidget;
        this.pc = pc;
        this.bookmarkStore = LevelDataHelper.getLevelData(level).bookmarkStore;
        this.addWidget = addWidget;
        this.removeWidget = removeWidget;
    }

    /**
     * Initialize all PC tab UI elements.
     * Call this during screen initialization.
     */
    public void initialize(int guiLeft, int guiTop) {
        createBookmarkButton(guiLeft, guiTop);
        createHomeButton(guiLeft, guiTop);
        rebuildTabButtons(guiLeft, guiTop);
    }

    /**
     * Update button states based on current box.
     * Call this every render frame.
     */
    public void update() {
        if (bookmarkButton != null) {
            int currentBox = storageWidget.getBox();
            boolean isBookmarked = bookmarkStore.has(currentBox);
            bookmarkButton.setActive(isBookmarked);
        }
    }

    /**
     * Clean up all managed buttons.
     * Call this when the screen is closed.
     */
    public void cleanup() {
        removeAllButtons();
    }

    // ========================================
    // Private implementation
    // ========================================

    private void createBookmarkButton(int guiLeft, int guiTop) {
        int x = guiLeft + BOOKMARK_BUTTON_OFFSET_X;
        int y = guiTop + BOOKMARK_BUTTON_OFFSET_Y;

        bookmarkButton = BookmarkButtons.createBookmarkButton(
                x, y,
                bookmarkStore.has(storageWidget.getBox()),
                btn -> handleBookmarkClick(guiLeft, guiTop));

        addWidget.accept(bookmarkButton);
    }

    private void createHomeButton(int guiLeft, int guiTop) {
        int x = guiLeft + HOME_BUTTON_OFFSET_X;
        int y = guiTop + HOME_BUTTON_OFFSET_Y;

        homeButton = BookmarkButtons.createHomeButton(
                x, y,
                btn -> storageWidget.setBox(0));

        addWidget.accept(homeButton);
    }

    private void rebuildTabButtons(int guiLeft, int guiTop) {
        // Remove old tab buttons
        tabButtons.forEach(removeWidget);
        tabButtons.clear();

        // Create new tab buttons
        int startX = guiLeft + TAB_START_OFFSET_X;
        int startY = guiTop + TAB_START_OFFSET_Y;

        List<PCButton> newButtons = BookmarkButtons.createTabButtons(
                bookmarkStore.getList(),
                pc.getBoxes(),
                startX,
                startY,
                storageWidget::setBox);

        // Add to screen and track
        newButtons.forEach(btn -> {
            addWidget.accept(btn);
            tabButtons.add(btn);
        });
    }

    private void handleBookmarkClick(int guiLeft, int guiTop) {
        int currentBox = storageWidget.getBox();

        bookmarkStore.toggle(currentBox);

        // Update button state
        bookmarkButton.toggleActive();

        // Rebuild tabs to show/hide the tab
        rebuildTabButtons(guiLeft, guiTop);
    }

    private void removeAllButtons() {
        if (bookmarkButton != null) {
            removeWidget.accept(bookmarkButton);
            bookmarkButton = null;
        }
        if (homeButton != null) {
            removeWidget.accept(homeButton);
            homeButton = null;
        }
        tabButtons.forEach(removeWidget);
        tabButtons.clear();
    }
}