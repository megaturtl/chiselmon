package cc.turtl.chiselmon.util.format;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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
     * Formats milliseconds into a readable format.
     */
    public static String formatDurationMs(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString();
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Capitalizes only the first letter of a string.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}