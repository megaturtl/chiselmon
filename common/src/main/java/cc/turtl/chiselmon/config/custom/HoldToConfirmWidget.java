package cc.turtl.chiselmon.config.custom;

import cc.turtl.chiselmon.util.format.ColorUtils;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

public class HoldToConfirmWidget extends ControllerWidget<HoldToConfirmController> {
    private static final float REGRESSION_MULTIPLIER = 2f;
    private static final int PROGRESS_COLOR = ColorUtils.argb(ColorUtils.RED.getRGB(), 0.75f);

    private final Set<Integer> heldKeys = new HashSet<>();
    private final int holdTimeTicks;
    private float progressTicks = 0f;

    public HoldToConfirmWidget(HoldToConfirmController controller, YACLScreen screen, Dimension<Integer> dim) {
        super(controller, screen, dim);
        this.holdTimeTicks = controller.option().holdTimeTicks();
    }

    private static boolean isActivationKey(int keyCode) {
        return keyCode == InputConstants.KEY_RETURN
                || keyCode == InputConstants.KEY_SPACE
                || keyCode == InputConstants.KEY_NUMPADENTER;
    }

    @Override
    protected Component getValueText() {
        if (!heldKeys.isEmpty() && progressTicks > 0) {
            return control.option().holdingText();
        }
        return control.option().buttonText();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        // Render progress bar
        if (progressTicks > 0f) {
            int barWidth = (int) ((progressTicks / holdTimeTicks) * (getDimension().width() - 2));
            graphics.fill(
                    getDimension().x() + 1,
                    getDimension().y() + 1,
                    getDimension().x() + 1 + barWidth,
                    getDimension().yLimit() - 1,
                    PROGRESS_COLOR
            );
        }

        // Update progress
        if (!heldKeys.isEmpty() && isAvailable()) {
            progressTicks = Math.min(holdTimeTicks, progressTicks + delta);
            if (progressTicks >= holdTimeTicks) {
                executeAction();
                progressTicks = 0f;
                heldKeys.clear();
            }
        } else {
            progressTicks = Math.max(0, progressTicks - REGRESSION_MULTIPLIER * delta);
        }

        // Clear held keys if not available or mouse left the button
        if (!isAvailable()) {
            heldKeys.clear();
        }
        if (heldKeys.contains(-1) && !isMouseOver(mouseX, mouseY)) {
            heldKeys.remove(-1);
        }
    }

    private void executeAction() {
        playDownSound();
        control.option().action().accept(screen, control.option());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && isAvailable()) {
            playDownSound();
            heldKeys.add(-1); // Use -1 for mouse clicks
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        heldKeys.remove(-1);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isAvailable() && !isMouseOver(mouseX, mouseY)) {
            heldKeys.remove(-1);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) {
            return false;
        }

        if (isActivationKey(keyCode)) {
            if (heldKeys.isEmpty()) {
                playDownSound();
            }
            heldKeys.add(keyCode);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isActivationKey(keyCode)) {
            heldKeys.remove(keyCode);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void unfocus() {
        super.unfocus();
        heldKeys.clear();
    }

    @Override
    protected int getHoveredControlWidth() {
        return getUnhoveredControlWidth();
    }

    @Override
    public boolean canReset() {
        return false;
    }
}