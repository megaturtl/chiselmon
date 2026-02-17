package cc.turtl.chiselmon.util.format;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Pure string and mathematical formatting utilities.
 * Does not contain any Minecraft-specific logic.
 */
public final class StringFormats {
    private StringFormats() {
    }

    /**
     * Converts a fraction to a percentage string.
     *
     * @param fraction 0.5 -> "50.00%"
     */
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }

    /**
     * Formats a decimal to two places.
     */
    public static String formatDecimal(double decimal) {
        return String.format("%.2f", decimal);
    }

    /**
     * Converts snake_case or SCREAMING_SNAKE_CASE to Title Case.
     * Example: "water_type" -> "Water Type"
     */
    public static String formatSnakeCase(String internalName) {
        if (internalName == null || internalName.isEmpty()) return "";

        return Arrays.stream(internalName.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Formats milliseconds into a readable mm:ss format.
     */
    public static String formatDurationMs(long milliseconds) {
        return DurationFormatUtils.formatDuration(milliseconds, "mm:ss");
    }

    /**
     * Capitalizes only the first letter of a string.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}