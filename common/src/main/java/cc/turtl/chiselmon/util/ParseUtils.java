package cc.turtl.chiselmon.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class ParseUtils {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]");

    public static String normalizeSpeciesName(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Normalize (Decompose characters like 'é' to 'e' + accent)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove accents
        String stripped = DIACRITICS.matcher(normalized).replaceAll("");

        // Lowercase and strip everything except a-z and 0-9
        return NON_ALPHANUMERIC.matcher(stripped.toLowerCase(Locale.ROOT)).replaceAll("");
    }
}