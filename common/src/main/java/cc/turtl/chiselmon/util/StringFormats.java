package cc.turtl.chiselmon.util;

public class StringFormats {
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }

    public static String formatDurationMs(long milliseconds) {
        final String DURATION_FORMAT = "mm:ss";
        return org.apache.commons.lang3.time.DurationFormatUtils.formatDuration(milliseconds, DURATION_FORMAT);
    }

    public static String formatTitleCase(String text) {
        return org.apache.commons.lang3.StringUtils.capitalize(text);
    }
}