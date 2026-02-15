package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.api.Priority;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable filter definition that's suitable for the config format.
 * Uses string tags to define filter conditions.
 */
public class FilterDefinition {

    @SerialEntry
    public String id;

    @SerialEntry
    public String displayName;

    @SerialEntry
    public Color color;

    @SerialEntry
    public Priority priority;

    @SerialEntry
    public boolean enabled;

    @SerialEntry
    public List<String> tags; // e.g., ["shiny", "type:fire", "species:pikachu", "size:0.1-0.5"]

    public FilterDefinition(String id, String displayName, Color color, Priority priority, boolean enabled, List<String> tags) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.priority = priority;
        this.enabled = enabled;
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}