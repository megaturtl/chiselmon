package cc.turtl.cobbleaid.util;

public class ColorUtil {
    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;
    public static final int LIGHT_GRAY = 0xAAAAAA;
    public static final int DARK_GRAY = 0x555555;

    public static final int RED = 0xF82553;
    public static final int ORANGE = 0xFB6640;
    public static final int YELLOW = 0xF8C421;
    public static final int GREEN = 0x49CC5C;
    public static final int BLUE = 0x2C7CE5;
    public static final int PURPLE = 0x7334E9;
    public static final int PINK = 0xE62CAB;

    public static final int CRIMSON = 0xDC143C;
    public static final int CORAL = 0xFF7F50;
    public static final int GOLD = 0xFFD700;
    public static final int LIME = 0x32CD32;
    public static final int CYAN = 0x00CED1;
    public static final int INDIGO = 0x4B0082;
    public static final int MAGENTA = 0xFF00FF;
    public static final int BROWN = 0x8B4513;
    public static final int TURQUOISE = 0x40E0D0;
    public static final int LAVENDER = 0xE6E6FA;
    public static final int MINT = 0x98FF98;
    public static final int PEACH = 0xFFDAB9;
    public static final int NAVY = 0x000080;
    public static final int MAROON = 0x800000;
    public static final int OLIVE = 0x808000;
    public static final int TEAL = 0x008080;

    public static int alphaColor(int color, double opacity) {
        int alphaValue = percentToAlpha(opacity);
        int alphaShifted = alphaValue << 24;
        return alphaShifted | color;
    }

    private static int percentToAlpha(double opacity) {
        double calculatedAlpha = opacity * 255.0;
        int roundedAlpha = (int) Math.round(calculatedAlpha);
        return Math.min(255, roundedAlpha);
    }

    public static int blendColor(int color1, int color2, double ratio) {
        double inverseRatio = 1.0 - ratio;
        int r = (int) (((color1 >> 16) & 0xFF) * inverseRatio + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int) (((color1 >> 8) & 0xFF) * inverseRatio + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int) ((color1 & 0xFF) * inverseRatio + (color2 & 0xFF) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    public static int getRatioGradientColor(double ratio) {
        return getRatioGradientColor(ratio, new int[] { RED, YELLOW, GREEN });
    }

    public static int getRatioGradientColor(double ratio, int[] colors) {
        // The gradient has N colors, meaning N-1 segments.
        int segmentCount = colors.length - 1;
        int percentageSegment = Math.max(0, Math.min((int) (ratio * segmentCount), segmentCount - 1));
        double percentageLocalRatio = (ratio * segmentCount) - percentageSegment;

        return blendColor(colors[percentageSegment], colors[percentageSegment + 1], percentageLocalRatio);
    }
}