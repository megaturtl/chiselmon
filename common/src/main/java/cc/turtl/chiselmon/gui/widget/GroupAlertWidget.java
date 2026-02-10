package cc.turtl.chiselmon.gui.widget;

import cc.turtl.chiselmon.config.option.GroupAlertOption;
import cc.turtl.chiselmon.system.alert.AlertSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

/**
 * Widget for displaying and editing a group's alert settings.
 * Includes collapsible details, checkboxes, sound selector with preview, and color picker.
 */
public class GroupAlertWidget extends AbstractWidget {

    private final GroupAlertOption option;
    private final Font font;
    private boolean expanded = false;

    // Sub-widgets for expanded state
    private Button enabledButton;
    private Button soundButton;
    private Button previewButton;
    private int expandedHeight = 80;

    public GroupAlertWidget(int x, int y, int width, int height, GroupAlertOption option, Font font) {
        super(x, y, width, height, Component.literal(option.getDisplayName()));
        this.option = option;
        this.font = font;
    }

    public GroupAlertOption getOption() {
        return option;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background
        int bgColor = this.isHovered() ? 0x80404040 : 0x80303030;
        graphics.fill(getX(), getY(), getX() + width, getY() + getCurrentHeight(), bgColor);

        // Border
        graphics.renderOutline(getX(), getY(), width, getCurrentHeight(), 0xFF606060);

        // Group name with status indicator
        int statusColor = option.isEnabled() ? 0x00FF00 : 0xFF5555;
        graphics.drawString(font, "●", getX() + 4, getY() + 7, statusColor);
        graphics.drawString(font, option.getDisplayName(), getX() + 16, getY() + 7, 0xFFFFFF);

        // Expand/collapse indicator
        String indicator = expanded ? "▼" : "▶";
        graphics.drawString(font, indicator, getX() + width - 15, getY() + 7, 0xAAAAAA);

        if (expanded) {
            renderExpandedContent(graphics, mouseX, mouseY);
        }
    }

    private void renderExpandedContent(GuiGraphics graphics, int mouseX, int mouseY) {
        int contentY = getY() + 24;
        int labelX = getX() + 10;
        int controlX = getX() + 100;

        // Enabled checkbox
        graphics.drawString(font, "Enabled:", labelX, contentY + 4, 0xAAAAAA);
        String enabledText = option.isEnabled() ? "Yes" : "No";
        int enabledColor = option.isEnabled() ? 0x00FF00 : 0xFF5555;
        graphics.drawString(font, "[" + enabledText + "]", controlX, contentY + 4, enabledColor);

        // Sound setting
        contentY += 16;
        graphics.drawString(font, "Sound:", labelX, contentY + 4, 0xAAAAAA);
        graphics.drawString(font, "[" + option.getSound().toString() + "]", controlX, contentY + 4, 0x55AAFF);

        // Preview button indicator
        graphics.drawString(font, "[▶ Play]", controlX + 80, contentY + 4, 0x55FF55);

        // Volume
        contentY += 16;
        graphics.drawString(font, "Volume:", labelX, contentY + 4, 0xAAAAAA);
        graphics.drawString(font, option.getVolume() + "%", controlX, contentY + 4, 0xFFFFFF);

        // Chat message
        contentY += 16;
        graphics.drawString(font, "Chat Msg:", labelX, contentY + 4, 0xAAAAAA);
        String chatText = option.shouldSendChatMessage() ? "Yes" : "No";
        int chatColor = option.shouldSendChatMessage() ? 0x00FF00 : 0xFF5555;
        graphics.drawString(font, "[" + chatText + "]", controlX, contentY + 4, chatColor);
    }

    private int getCurrentHeight() {
        return expanded ? expandedHeight : height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int relativeY = (int) mouseY - getY();

        if (relativeY < 24) {
            // Clicked on header - toggle expand
            expanded = !expanded;
        } else if (expanded) {
            // Handle clicks on expanded content
            int relativeX = (int) mouseX - getX();
            int contentY = 24;

            // Enabled toggle
            if (relativeY >= contentY && relativeY < contentY + 16 && relativeX >= 100) {
                option.setEnabled(!option.isEnabled());
                return;
            }
            contentY += 16;

            // Sound selector
            if (relativeY >= contentY && relativeY < contentY + 16) {
                if (relativeX >= 180) {
                    // Preview button clicked
                    playPreviewSound();
                } else if (relativeX >= 100) {
                    // Cycle sound
                    cycleSoundOption();
                }
                return;
            }
            contentY += 16;

            // Volume (could implement slider later)
            if (relativeY >= contentY && relativeY < contentY + 16 && relativeX >= 100) {
                // Cycle volume: 25, 50, 75, 100
                int current = option.getVolume();
                int next = current >= 100 ? 25 : current + 25;
                option.setVolume(next);
                return;
            }
            contentY += 16;

            // Chat message toggle
            if (relativeY >= contentY && relativeY < contentY + 16 && relativeX >= 100) {
                option.setSendChatMessage(!option.shouldSendChatMessage());
                return;
            }
        }
    }

    private void cycleSoundOption() {
        AlertSounds[] sounds = AlertSounds.values();
        int currentIndex = option.getSound().ordinal();
        int nextIndex = (currentIndex + 1) % sounds.length;
        option.setSound(sounds[nextIndex]);
    }

    private void playPreviewSound() {
        SoundEvent sound = option.getSound().getSound();
        if (sound != null) {
            float volume = option.getVolume() / 100f;
            float pitch = option.getPitch() / 100f;
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(sound, pitch, volume)
            );
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarrationElementOutput.Type.TITLE, Component.literal(option.getDisplayName()));
    }

    public void setY(int y) {
        this.setPosition(this.getX(), y);
    }
}
