package cc.turtl.cobbleaid.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class StringUtils {
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }

    public static String formatDecimal(double decimal) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(decimal);
    }

    public static String formatDisplayName(String internalName) {
        return Arrays.stream(internalName.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String formatDurationMs(long milliseconds) {
        final String DURATION_FORMAT = "mm:ss";
        return DurationFormatUtils.formatDuration(milliseconds, DURATION_FORMAT);
    }

    public static String formatTitleCase(String text) {
        return org.apache.commons.lang3.StringUtils.capitalize(text);
    }

}