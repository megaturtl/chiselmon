package cc.turtl.chiselmon.util.format;

import net.minecraft.ChatFormatting;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

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
    public static final Color GREEN = new Color(0x4DBA51);
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
    public static final Color PEACH = new Color(0xFFDAB9);
    public static final Color NAVY = new Color(0x000080);
    public static final Color TEAL = new Color(0x008080);
    private static final int[] MC_PALETTE = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private ColorUtils() {
    }

    /**
     * Converts an RGB color to an ARGB integer with custom transparency.
     * * @param rgb     The source color (0xRRGGBB)
     *
     * @param opacity The alpha percentage (0.0 to 1.0)
     * @return ARGB (0xAARRGGBB)
     */
    public static int argb(int rgb, float opacity) {
        return FastColor.ARGB32.color(FastColor.as8BitChannel(opacity), rgb);
    }

    /**
     * Finds the closest vanilla {@link ChatFormatting} color based on perceptual distance.
     * * @param rgb The source color (0xRRGGBB)
     *
     * @return The closest matching legacy format
     */
    public static ChatFormatting legacy(int rgb) {
        return getClosestFormat(rgb);
    }

    /**
     * Calculates a color at a specific point along a multi-color gradient.
     * * @param ratio  The position in the gradient (0.0 to 1.0)
     *
     * @param colors The RGB color stops defining the gradient
     * @return The interpolated ARGB color
     */
    public static int getGradient(float ratio, int... colors) {
        if (colors.length == 0) return 0xFFFFFFFF;
        if (colors.length == 1) return argb(colors[0], 1);

        ratio = Mth.clamp(ratio, 0.0F, 1.0F);
        float segmentSize = 1.0F / (colors.length - 1);
        int segment = Mth.clamp((int) (ratio / segmentSize), 0, colors.length - 2);
        float localRatio = (ratio - (segment * segmentSize)) / segmentSize;

        return FastColor.ARGB32.lerp(localRatio, colors[segment], argb(colors[segment + 1], 1));
    }

    private static ChatFormatting getClosestFormat(int rgb) {
        int r = FastColor.ARGB32.red(rgb);
        int g = FastColor.ARGB32.green(rgb);
        int b = FastColor.ARGB32.blue(rgb);

        int closestIndex = 0;
        long minDistance = Long.MAX_VALUE;

        for (int i = 0; i < MC_PALETTE.length; i++) {
            int pr = FastColor.ARGB32.red(MC_PALETTE[i]);
            int pg = FastColor.ARGB32.green(MC_PALETTE[i]);
            int pb = FastColor.ARGB32.blue(MC_PALETTE[i]);

            int dr = r - pr;
            int dg = g - pg;
            int db = b - pb;

            // Perceptual weighting: Human eyes perceive Green more strongly than Red or Blue.
            long distance = (2L * dr * dr) + (4L * dg * dg) + (3L * db * db);

            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return ChatFormatting.getById(closestIndex);
    }
}