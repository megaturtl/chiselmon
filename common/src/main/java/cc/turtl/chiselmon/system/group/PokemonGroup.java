package cc.turtl.chiselmon.system.group;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

/**
 * Represents a named group of Pokemon matching specific criteria.
 *
 * @param id Unique identifier for this group (e.g., "legendaries", "custom_tiny_shinies")
 * @param name Display name for UI (e.g., "Legendary Pokemon", "Tiny Shinies")
 * @param rgb Color used for text and glow effects involving the group.
 * @param priority Alert priority for this group
 * @param condition Predicate that determines if a Pokemon belongs to this group
 */
public record PokemonGroup(
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
        return ComponentUtils.createComponent(name, rgb);
    }
}