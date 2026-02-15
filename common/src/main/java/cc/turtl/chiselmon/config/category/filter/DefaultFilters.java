package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.api.Priority;

import java.awt.*;
import java.util.List;

public class DefaultFilters {

    public static FilterDefinition legendaries() {
        return new FilterDefinition(
                "legendaries",
                "Legendary Pokemon",
                Color.MAGENTA,
                Priority.HIGHEST,
                true,
                List.of("legendary")
        );
    }

    public static FilterDefinition shinies() {
        return new FilterDefinition(
                "shinies",
                "Shiny Pokemon",
                Color.ORANGE,
                Priority.HIGH,
                true,
                List.of("shiny")
        );
    }

    public static FilterDefinition extremeSizes() {
        return new FilterDefinition(
                "extreme_sizes",
                "Extreme Size Pokemon",
                Color.CYAN,
                Priority.NORMAL,
                true,
                List.of("extreme_size")
        );
    }

    public static List<FilterDefinition> all() {
        return List.of(legendaries(), shinies(), extremeSizes());
    }
}