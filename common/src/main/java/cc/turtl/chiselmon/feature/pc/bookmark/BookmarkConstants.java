package cc.turtl.chiselmon.feature.pc.bookmark;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

import cc.turtl.chiselmon.util.format.ComponentUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;

/**
 * Configuration constants for PC Bookmark Buttons.
 */
public class BookmarkConstants {

    // Bookmark button
    public static final ResourceLocation BOOKMARK_SPRITE = modResource("textures/gui/pc/pc_button_bookmark.png");
    public static final int BOOKMARK_TEXTURE_WIDTH = 15;
    public static final int BOOKMARK_TEXTURE_HEIGHT = 30;
    public static final Tooltip BOOKMARK_ACTIVE_TOOLTIP =
            Tooltip.create(ComponentUtils.modTranslatable("pc.bookmark_button.tooltip.remove"));
    public static final Tooltip BOOKMARK_INACTIVE_TOOLTIP =
            Tooltip.create(ComponentUtils.modTranslatable("pc.bookmark_button.tooltip.add"));

    // Home button
    public static final ResourceLocation HOME_SPRITE = modResource("textures/gui/pc/pc_button_home.png");
    public static final int HOME_TEXTURE_WIDTH = 15;
    public static final int HOME_TEXTURE_HEIGHT = 30;
    public static final Tooltip HOME_TOOLTIP =
            Tooltip.create(ComponentUtils.modTranslatable("pc.home_button.tooltip"));

    // Tab button
    public static final ResourceLocation TAB_SPRITE = modResource("textures/gui/pc/pc_button_tab.png");
    public static final int TAB_TEXTURE_WIDTH = 35;
    public static final int TAB_TEXTURE_HEIGHT = 20;
    public static final int TAB_TEXT_MARGIN = 5;

    private BookmarkConstants() {
    }
}