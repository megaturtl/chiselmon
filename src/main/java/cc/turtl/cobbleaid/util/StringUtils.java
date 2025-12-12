package cc.turtl.cobbleaid.util;

public class StringUtils {
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }
}