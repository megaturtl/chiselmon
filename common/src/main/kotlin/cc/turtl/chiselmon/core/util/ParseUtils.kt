package cc.turtl.chiselmon.core.util

import java.text.Normalizer
import java.util.regex.Pattern

private val DIACRITICS: Pattern = Pattern.compile("\\p{M}")
private val NON_ALPHANUMERIC: Pattern = Pattern.compile("[^a-z0-9]")

fun normalizeSpeciesName(input: String?): String {
    if (input.isNullOrBlank()) {
        return ""
    }

    // Normalize (Decompose characters like 'é' to 'e' + accent)
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)

    // Remove accents
    val stripped = DIACRITICS.matcher(normalized).replaceAll("")

    // Lowercase and strip everything except a-z and 0-9
    return NON_ALPHANUMERIC.matcher(stripped.lowercase()).replaceAll("")
}