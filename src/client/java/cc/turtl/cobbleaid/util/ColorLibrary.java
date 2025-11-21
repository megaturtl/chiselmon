package cc.turtl.cobbleaid.util;

public class ColorLibrary {
    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;

    public static final int GRAY = 0x808080;
    public static final int LIGHT_GRAY = 0xC0C0C0;
    public static final int DARK_GRAY = 0x404040;

    public static final int RED = 0xFF0000;
    public static final int ORANGE = 0xFFA500;
    public static final int YELLOW = 0xFFFF00;
    public static final int GREEN = 0x00FF00;
    public static final int BLUE = 0x0000FF;
    public static final int PURPLE = 0x800080;

    public static final int ALPHA_100 = 0xFF;
    public static final int ALPHA_75 = 0xBF;
    public static final int ALPHA_50 = 0x80;
    public static final int ALPHA_25 = 0x40;
    public static final int ALPHA_0 = 0x00;

    public static int withAlpha(int alpha, int color) {
        return (alpha << 24) | color;
    }
}
