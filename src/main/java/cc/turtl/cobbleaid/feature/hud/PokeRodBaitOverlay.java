package cc.turtl.cobbleaid.feature.hud;

import com.cobblemon.mod.common.item.interactive.PokerodItem;

import cc.turtl.cobbleaid.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PokeRodBaitOverlay {

    public static void renderPokeRodOverlay(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null)
            return;

        // Get the currently held item
        ItemStack heldItem = player.getMainHandItem();

        // Check if it's a Poke Rod
        if (!(heldItem.getItem() instanceof PokerodItem))
            return;

        // Get the bait stack from the rod
        ItemStack baitStack = PokerodItem.Companion.getBaitStackOnRod(heldItem);

        // If there's no bait, optionally show "No Bait" or return
        if (baitStack.isEmpty()) {
            renderBaitText(guiGraphics, minecraft, "No Bait!", 0, ColorUtil.RED);
            return;
        }

        // Get bait name and count
        String baitName = baitStack.getHoverName().getString();
        int baitCount = baitStack.getCount();
        int rgb = ColorUtil.getRatioGradientColor(baitCount / 64.0f);

        renderBaitTextWithIcon(guiGraphics, minecraft, baitName, baitCount, rgb, baitStack);
    }

    private static void renderBaitText(GuiGraphics guiGraphics, Minecraft minecraft, String baitName, int count, int color) {
        Font font = minecraft.font;

        // Format the text
        String displayText;
        if (count > 0) {
            displayText = baitName + " x" + count;
        } else {
            displayText = baitName;
        }

        // Calculate text width (i)
        int textWidth = font.width(displayText);

        // Calculate X position (j): Centered
        int x = (guiGraphics.guiWidth() - textWidth) / 2;

        // Calculate initial Y position (k): Above hotbar (guiHeight - 59)
        int y = guiGraphics.guiHeight() - 59;

        // Shift down if health bar doesn't show (creative/spectator)
        if (minecraft.player != null && minecraft.player.isCreative() && !minecraft.gameMode.canHurtPlayer()) {
            y += 14;
        }

        // Render with shadow for better visibility
        guiGraphics.drawString(font, displayText, x, y, color, true);
    }

    private static void renderBaitTextWithIcon(GuiGraphics guiGraphics, Minecraft minecraft, String baitName, int count,
            int color, ItemStack baitStack) {
        Font font = minecraft.font;

        // Format the text
        String displayText;
        if (count > 0) {
            displayText = baitName + " x" + count;
        } else {
            displayText = baitName;
        }

        // Calculate dimensions
        int textWidth = font.width(displayText);
        int iconSize = 16; // Standard item icon size
        int spacing = 3; // Space between icon and text
        int totalWidth = iconSize + spacing + textWidth;

        // Center the entire display (icon + text) (j logic applied to totalWidth)
        int startX = (guiGraphics.guiWidth() - totalWidth) / 2;

        // Calculate initial Y position (k): Above hotbar (guiHeight - 59)
        int y = guiGraphics.guiHeight() - 59;

        // Shift down if health bar doesn't show (creative/spectator)
        if (minecraft.player != null && minecraft.player.isCreative() && !minecraft.gameMode.canHurtPlayer()) {
            y += 14;
        }

        // Adjust the icon position to align the text's baseline with the calculated Y.
        // Standard text draws from the top, the icon is 16px high.
        int iconY = y - 4; // Adjusted for better visual alignment with the text

        // Render the item icon
        guiGraphics.renderItem(baitStack, startX, iconY);

        // Render the text after the icon
        int textX = startX + iconSize + spacing;
        guiGraphics.drawString(font, displayText, textX, y, color, true);
    }
}