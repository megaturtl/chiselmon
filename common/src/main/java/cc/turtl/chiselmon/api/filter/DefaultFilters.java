package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ColorUtils;

import java.util.List;

public class DefaultFilters {

    public static FilterDefinition legendaries() {
        return new FilterDefinition(
                "legendaries",
                "Legendary Pokemon",
                ColorUtils.MAGENTA,
                Priority.HIGHEST,
                true,
                List.of("legendary")
        );
    }

    public static FilterDefinition shinies() {
        return new FilterDefinition(
                "shinies",
                "Shiny Pokemon",
                ColorUtils.GOLD,
                Priority.HIGH,
                true,
                List.of("shiny")
        );
    }

    public static FilterDefinition extremeSizes() {
        return new FilterDefinition(
                "extreme_sizes",
                "Extreme Size Pokemon",
                ColorUtils.TEAL,
                Priority.NORMAL,
                true,
                List.of("extreme_size")
        );
    }

    public static List<FilterDefinition> all() {
        return List.of(legendaries(), shinies(), extremeSizes());
    }
}