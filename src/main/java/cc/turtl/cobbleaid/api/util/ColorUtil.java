package cc.turtl.cobbleaid.api.util;

import net.minecraft.ChatFormatting;

public class ColorUtil {
    public static final int[] COLORS = {
            ChatFormatting.RED.getColor(),
            ChatFormatting.YELLOW.getColor(),
            ChatFormatting.GREEN.getColor()
    };

    public static int blendColor(int color1, int color2, double ratio) {
        double inverseRatio = 1.0 - ratio;
        int r = (int) (((color1 >> 16) & 0xFF) * inverseRatio + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int) (((color1 >> 8) & 0xFF) * inverseRatio + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int) ((color1 & 0xFF) * inverseRatio + (color2 & 0xFF) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    public static int getRatioGradientColor(double ratio) {
        return getRatioGradientColor(ratio, COLORS);
    }
    public static int getRatioGradientColor(double ratio, int[] colors) {
        // The gradient has N colors, meaning N-1 segments.
        int segmentCount = colors.length - 1;
        int percentageSegment = Math.max(0, Math.min((int) (ratio * segmentCount), segmentCount - 1));
        double percentageLocalRatio = (ratio * segmentCount) - percentageSegment;

        return blendColor(colors[percentageSegment], colors[percentageSegment + 1], percentageLocalRatio);
    }
}