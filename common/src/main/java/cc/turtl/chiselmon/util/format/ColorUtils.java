package cc.turtl.chiselmon.util.format;

import java.awt.*;

public final class ColorUtils {
    // --- Basic Colors ---
    public static final Color WHITE = new Color(0xFFFFFF);
    public static final Color BLACK = new Color(0x000000);
    public static final Color LIGHT_GRAY = new Color(0xAAAAAA);
    public static final Color DARK_GRAY = new Color(0x555555);
    // --- Rainbow Palette ---
    public static final Color RED = new Color(0xE13538);
    public static final Color ORANGE = new Color(0xF9844A);
    public static final Color YELLOW = new Color(0xF9C74F);
    public static final Color GREEN = new Color(0x41D73B);
    public static final Color BLUE = new Color(0x2D73B0);
    public static final Color PURPLE = new Color(0x6C44C3);
    public static final Color PINK = new Color(0xF46997);
    // --- Extended Palette ---
    public static final Color CRIMSON = new Color(0xDC143C);
    public static final Color CORAL = new Color(0xFF7F50);
    public static final Color GOLD = new Color(0xFFD700);
    public static final Color LIME = new Color(0x32CD32);
    public static final Color INDIGO = new Color(0x4B0082);
    public static final Color MAGENTA = new Color(0xFF00FF);
    public static final Color BROWN = new Color(0x8B4513);
    public static final Color AQUA = new Color(0x40E0D0);
    public static final Color LAVENDER = new Color(0xDEDEFC);
    public static final Color MINT = new Color(0x98FF98);
    public static final Color TEAL = new Color(0x008080);
    private static final int[] MC_PALETTE = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private ColorUtils() {
    }
}