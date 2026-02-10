package cc.turtl.chiselmon.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Base screen class for Chiselmon GUI screens.
 * Provides common functionality and styling.
 */
public abstract class ChiselmonScreen extends Screen {

    protected final Screen parent;

    protected ChiselmonScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        // Add common elements like back button
        addDoneButton();
    }

    protected void addDoneButton() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Draws a tooltip at the mouse position
     */
    protected void renderTooltip(GuiGraphics graphics, Component text, int mouseX, int mouseY) {
        graphics.renderTooltip(this.font, text, mouseX, mouseY);
    }
}
