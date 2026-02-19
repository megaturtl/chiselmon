package cc.turtl.chiselmon.config.custom;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyWidget extends ControllerWidget<KeyController> {
    private boolean listening = false;

    // Middle click and other mouse buttons are consumed before reaching
    // this widget, so we poll GLFW directly for these during render instead
    private static final int[] POLLED_BUTTONS = {
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            GLFW.GLFW_MOUSE_BUTTON_4,
            GLFW.GLFW_MOUSE_BUTTON_5,
    };

    public KeyWidget(KeyController controller, YACLScreen screen, Dimension<Integer> dim) {
        super(controller, screen, dim);
    }

    @Override
    protected Component getValueText() {
        if (listening) {
            return Component.literal("> Press a key <");
        }
        return control.option().pendingValue().getDisplayName();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        if (!listening) return;

        long window = Minecraft.getInstance().getWindow().getWindow();
        for (int button : POLLED_BUTTONS) {
            if (GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS) {
                control.option().requestSet(InputConstants.Type.MOUSE.getOrCreate(button));
                playDownSound();
                listening = false;
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || !isAvailable()) return false;

        // If already listening, any mouse event that makes it here (left/right) binds the key.
        // Middle and side buttons never reach here so they are handled via polling in the render method.
        if (listening) {
            control.option().requestSet(InputConstants.Type.MOUSE.getOrCreate(button));
            listening = false;
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            playDownSound();
            listening = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listening) return false;

        // ESC clears the binding rather than exiting listen mode with no change
        if (keyCode == InputConstants.KEY_ESCAPE) {
            control.option().requestSet(InputConstants.UNKNOWN);
        } else {
            control.option().requestSet(InputConstants.getKey(keyCode, scanCode));
        }
        listening = false;
        return true;
    }

    @Override
    public void unfocus() {
        super.unfocus();
        listening = false;
    }

    @Override
    protected int getHoveredControlWidth() {
        return getUnhoveredControlWidth();
    }

    @Override
    public boolean canReset() { return true; }
}