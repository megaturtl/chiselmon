package cc.turtl.chiselmon.api.filter;

import cc.turtl.turtlshell.api.core.Priority;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

import static cc.turtl.chiselmon.core.util.format.ComponentUtilsKt.createComponent;

/**
 * Represents a named group of conditions specifying pokemon.
 *
 * @param id        Unique identifier for this filter (e.g., "legendaries", "custom_tiny_shinies")
 * @param name      Pretty display name string
 * @param rgb       RGB hex color used for text and glow effects involving the group.
 * @param priority  Alert priority for this filter
 * @param condition Predicate that determines if a Pokemon passes this filter
 */
public record RuntimeFilter(
        String id,
        String name,
        int rgb,
        Priority priority,
        Predicate<Pokemon> condition
) {
    public boolean matches(Pokemon pokemon) {
        return condition.test(pokemon);
    }

    public Component displayName() {
        return createComponent(name(), rgb(), false);
    }
}