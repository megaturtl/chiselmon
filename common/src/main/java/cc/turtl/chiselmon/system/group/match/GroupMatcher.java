package cc.turtl.chiselmon.system.group.match;

import cc.turtl.chiselmon.system.group.PokemonGroup;
import cc.turtl.chiselmon.system.group.PokemonGroupRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.Optional;

public class GroupMatcher {
    private final PokemonGroupRegistry registry;

    public GroupMatcher(PokemonGroupRegistry registry) {
        this.registry = registry;
    }

    public GroupMatchResult match(Pokemon pokemon) {
        List<PokemonGroup> sortedGroups = registry.getSorted();

        List<PokemonGroup> matches = sortedGroups.stream()
                .filter(group -> group.condition().test(pokemon))
                .toList();

        // Since the list was already sorted, the first match in the list is the highest priority
        Optional<PokemonGroup> primary = matches.isEmpty()
                ? Optional.empty()
                : Optional.of(matches.getFirst());

        return new GroupMatchResult(
                pokemon,
                primary,
                matches
        );
    }
}