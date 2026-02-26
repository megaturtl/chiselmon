package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ColorUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializable filter definition suitable for the config format.
 *
 * <p>The condition is stored as a plain string using word-operator syntax
 * (e.g. {@code "shiny AND type=fire"}), parsed into a {@link FilterCondition}
 * tree at runtime by {@link FilterConditionParser}. This means no custom Gson
 * adapter is needed — the field serializes as a simple JSON string.
 *
 * <p><b>Migration:</b> Older saves may have a {@code tags} list instead of
 * {@code conditionString}. {@link #migrateIfNeeded()} handles this automatically.
 */
public class FilterDefinition {

    public String id;
    public String displayName;
    public int rgb;
    public Priority priority;

    /** The filter condition as a word-operator string, e.g. "shiny AND type=fire". */
    public String conditionString;

    /**
     * Legacy field — kept for migration only.
     * Gson populates this if the JSON still has a {@code "tags"} array.
     *
     * @deprecated Use {@link #conditionString} instead.
     */
    @Deprecated
    public List<String> tags;

    public FilterDefinition(String id, String displayName, int rgb, Priority priority,
                            String conditionString) {
        this.id = id;
        this.displayName = displayName;
        this.rgb = rgb;
        this.priority = priority;
        this.conditionString = conditionString;
    }

    /**
     * Migrates from the old {@code tags} list to a {@code conditionString}.
     * No-op if {@code conditionString} is already set.
     */
    public void migrateIfNeeded() {
        if (conditionString != null) return;

        if (tags != null && !tags.isEmpty()) {
            conditionString = String.join(" AND ", tags);
        } else {
            conditionString = "";
        }
        tags = null;
    }

    // -------------------------------------------------------------------------
    // Default filters
    // -------------------------------------------------------------------------

    public static class DefaultFilters {

        public static final FilterDefinition LEGENDARIES = new FilterDefinition(
                "legendaries", "Legendary Pokemon",
                ColorUtils.MAGENTA.getRGB(), Priority.HIGHEST,
                "legendary"
        );

        public static final FilterDefinition SHINIES = new FilterDefinition(
                "shinies", "Shiny Pokemon",
                ColorUtils.GOLD.getRGB(), Priority.HIGH,
                "shiny"
        );

        public static final FilterDefinition EXTREME_SIZES = new FilterDefinition(
                "extreme_sizes", "Extreme Size Pokemon",
                ColorUtils.TEAL.getRGB(), Priority.NORMAL,
                "extreme_size"
        );

        private static final List<FilterDefinition> ALL = List.of(LEGENDARIES, SHINIES, EXTREME_SIZES);

        public static Map<String, FilterDefinition> all() {
            return ALL.stream().collect(Collectors.toUnmodifiableMap(f -> f.id, f -> f));
        }
    }
}