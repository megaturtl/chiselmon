package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializable filter definition that's suitable for the config format.
 * Uses string tags to define filter conditions.
 */
public class FilterDefinition {

    public String id;

    public String displayName;

    public int rgb;

    public Priority priority;

    public boolean enabled;

    public List<String> tags; // e.g., ["shiny", "type:fire", "species:pikachu", "size:0.1-0.5"]

    public FilterDefinition(String id, String displayName, int rgb, Priority priority, boolean enabled, List<String> tags) {
        this.id = id;
        this.displayName = displayName;
        this.rgb = rgb;
        this.priority = priority;
        this.enabled = enabled;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public static class DefaultFilters {
        public static final FilterDefinition LEGENDARIES = new FilterDefinition(
                "legendaries", "Legendary Pokemon",
                ColorUtils.MAGENTA.getRGB(), Priority.HIGHEST, true,
                List.of("legendary")
        );
        public static final FilterDefinition SHINIES = new FilterDefinition(
                "shinies", "Shiny Pokemon",
                ColorUtils.GOLD.getRGB(), Priority.HIGH, true,
                List.of("shiny")
        );
        public static final FilterDefinition EXTREME_SIZES = new FilterDefinition(
                "extreme_sizes", "Extreme Size Pokemon",
                ColorUtils.TEAL.getRGB(), Priority.NORMAL, true,
                List.of("extreme_size")
        );

        private static final List<FilterDefinition> ALL = List.of(LEGENDARIES, SHINIES, EXTREME_SIZES);

        public static Map<String, FilterDefinition> all() {
            return ALL.stream().collect(Collectors.toUnmodifiableMap(f -> f.id, f -> f));
        }
    }
}