package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Predicate;

/**
 * Represents a named group of conditions specifying pokemon.
 *
 * @param id        Unique identifier for this filter (e.g., "legendaries", "custom_tiny_shinies")
 * @param color       Color used for text and glow effects involving the group.
 * @param priority  Alert priority for this filter
 * @param condition Predicate that determines if a Pokemon passes this filter
 */
public record RuntimeFilter(
        String id,
        Color color,
        Priority priority,
        Predicate<Pokemon> condition
) {
    public boolean matches(Pokemon pokemon) {
        return condition.test(pokemon);
    }

    public Component displayName() {
        return ComponentUtils.createComponent(id(), color.getRGB());
    }
}