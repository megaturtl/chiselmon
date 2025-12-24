package cc.turtl.cobbleaid.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {
    public static String formatPercentage(double fraction) {
        return String.format("%.2f%%", fraction * 100.0);
    }

    public static String formatDisplayName(String internalName) {
        return Arrays.stream(internalName.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
    }

}