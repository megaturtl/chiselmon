package cc.turtl.chiselmon.core.api.filter

import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.turtlshell.api.core.Priority

/**
 * Serializable filter definition suitable for the config format.
 *
 * The condition is stored as a plain string using word-operator syntax
 * (e.g. "shiny AND type=fire"), parsed into a [FilterCondition] tree at
 * runtime by [FilterConditionParser]. No custom Gson adapter is needed --
 * the field serializes as a simple JSON string.
 */
class FilterDefinition(
    @JvmField var id: String,
    @JvmField var displayName: String,
    @JvmField var rgb: Int,
    @JvmField var priority: Priority,
    /** The filter condition as a word-operator string, e.g. "shiny AND type=fire". */
    @JvmField var conditionString: String
) {
    /**
     * Legacy field kept for migration only.
     * Gson populates this if the JSON still has a "tags" array.
     */
    @Deprecated("Use conditionString instead")
    @JvmField
    var tags: List<String>? = null

    // -------------------------------------------------------------------------
    // Default filters
    // -------------------------------------------------------------------------

    object DefaultFilters {

        @JvmField
        val LEGENDARIES = FilterDefinition(
            "legendaries", "Legendary Pokemon",
            ColorUtils.MAGENTA.rgb, Priority.HIGHEST,
            "legendary"
        )

        @JvmField
        val SHINIES = FilterDefinition(
            "shinies", "Shiny Pokemon",
            ColorUtils.GOLD.rgb, Priority.HIGH,
            "shiny"
        )

        @JvmField
        val EXTREME_SIZES = FilterDefinition(
            "extreme_sizes", "Extreme Size Pokemon",
            ColorUtils.TEAL.rgb, Priority.NORMAL,
            "extreme_size"
        )

        private val ALL = listOf(LEGENDARIES, SHINIES, EXTREME_SIZES)

        @JvmStatic
        fun all(): Map<String, FilterDefinition> = ALL.associateBy { it.id }
    }
}