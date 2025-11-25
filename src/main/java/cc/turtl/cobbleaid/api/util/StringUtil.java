package cc.turtl.cobbleaid.api.util;

public class StringUtil {
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }
}