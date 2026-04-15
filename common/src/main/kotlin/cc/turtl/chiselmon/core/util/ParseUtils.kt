package cc.turtl.chiselmon.core.util

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{M}")
private val NON_ALPHANUMERIC = Regex("[^a-z0-9]")

fun normalizeSpeciesName(input: String?): String {
    if (input.isNullOrBlank()) return ""

    // Decompose characters like 'é' into base letter + combining accent, then strip accents
    val stripped = DIACRITICS.replace(Normalizer.normalize(input, Normalizer.Form.NFD), "")

    return NON_ALPHANUMERIC.replace(stripped.lowercase(), "")
}
