package cc.turtl.chiselmon.gui.screen;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.ChiselmonConfigNew;
import cc.turtl.chiselmon.config.option.GroupAlertOption;
import cc.turtl.chiselmon.gui.widget.GroupAlertWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Main configuration screen for Chiselmon.
 * Displays all config categories with dynamic group support.
 */
public class ChiselmonConfigScreen extends ChiselmonScreen {

    private static final int PADDING = 8;
    private static final int ENTRY_HEIGHT = 24;

    private final List<GroupAlertWidget> groupWidgets = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public ChiselmonConfigScreen(Screen parent) {
        super(Component.literal("Chiselmon Configuration"), parent);
    }

    @Override
    protected void init() {
        super.init();

        // Sync groups from registry first
        ChiselmonConfigNew.get().alert().syncGroupsFromRegistry();

        // Create widgets for each group
        createGroupWidgets();

        // Calculate max scroll
        int contentHeight = groupWidgets.size() * (ENTRY_HEIGHT + PADDING);
        int viewportHeight = this.height - 80; // Account for title and done button
        maxScroll = Math.max(0, contentHeight - viewportHeight);
    }

    private void createGroupWidgets() {
        groupWidgets.clear();

        int y = 40;
        for (GroupAlertOption option : ChiselmonConfigNew.get().alert().getGroupAlerts()) {
            GroupAlertWidget widget = new GroupAlertWidget(
                    this.width / 2 - 150,
                    y,
                    300,
                    ENTRY_HEIGHT,
                    option,
                    this.font
            );
            groupWidgets.add(widget);
            this.addRenderableWidget(widget);
            y += ENTRY_HEIGHT + PADDING;
        }
    }

    @Override
    protected void addDoneButton() {
        // Save button
        this.addRenderableWidget(Button.builder(Component.literal("Save & Close"), button -> {
            ChiselmonConfigNew.get().save();
            onClose();
        }).bounds(this.width / 2 - 154, this.height - 27, 150, 20).build());

        // Reset button
        this.addRenderableWidget(Button.builder(Component.literal("Reset to Defaults"), button -> {
            for (GroupAlertWidget widget : groupWidgets) {
                widget.getOption().resetToDefault();
            }
            rebuildWidgets();
        }).bounds(this.width / 2 + 4, this.height - 27, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Render section header
        graphics.drawString(this.font, "Alert Groups", this.width / 2 - 150, 30, 0xAAAAAA);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render tooltips on hover
        for (GroupAlertWidget widget : groupWidgets) {
            if (widget.isHovered()) {
                String comment = widget.getOption().getComment();
                if (comment != null) {
                    renderTooltip(graphics, Component.literal(comment), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Handle scrolling
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
        updateWidgetPositions();
        return true;
    }

    private void updateWidgetPositions() {
        int y = 40 - scrollOffset;
        for (GroupAlertWidget widget : groupWidgets) {
            widget.setY(y);
            y += ENTRY_HEIGHT + PADDING;
        }
    }

    @Override
    public void removed() {
        super.removed();
        // Save config when screen closes
        ChiselmonConfigNew.get().save();
        ChiselmonConstants.LOGGER.info("[ChiselmonConfigScreen] Config saved on screen close");
    }
}
