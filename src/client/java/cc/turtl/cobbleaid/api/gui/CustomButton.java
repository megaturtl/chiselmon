package cc.turtl.cobbleaid.api.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import cc.turtl.cobbleaid.util.ColorLibrary;
import net.minecraft.network.chat.Component;

public class CustomButton extends Button {

    public int backgroundColor = ColorLibrary.withAlpha(ColorLibrary.ALPHA_100, ColorLibrary.DARK_GRAY);;
    public int backgroundHoverColor = ColorLibrary.withAlpha(ColorLibrary.ALPHA_100, ColorLibrary.GRAY);;
    public int borderColor = ColorLibrary.withAlpha(ColorLibrary.ALPHA_100, ColorLibrary.BLACK);;
    public int textColor = ColorLibrary.withAlpha(ColorLibrary.ALPHA_100, ColorLibrary.WHITE);
    public int textHoverColor = ColorLibrary.withAlpha(ColorLibrary.ALPHA_100, ColorLibrary.WHITE);
    public boolean hasBorder = true;
    public boolean isToggled = false;
    
    public CustomButton(int buttonX, int buttonY, int buttonWidth, int buttonHeight, Component message, OnPress clickAction) {
        super(buttonX, buttonY, buttonWidth, buttonHeight, message, clickAction, Button.DEFAULT_NARRATION);
    }

    public boolean isToggled() {
        return this.isToggled;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int bgColor = this.isHovered() ? this.backgroundHoverColor : this.backgroundColor;
        int textColor = this.isHovered() ? this.textHoverColor : this.textColor;

        GuiUtils.drawButtonBackground(context, this.getX(), this.getY(), this.width, this.height,
                this.borderColor, bgColor, this.hasBorder);

        GuiUtils.drawScaledText(context, getMessage(), this.getX(), this.getY(),
                this.width, this.height, textColor, false);
    }

    @Override
    public void onPress() {
        this.isToggled = !this.isToggled;
        super.onPress();
    }
}