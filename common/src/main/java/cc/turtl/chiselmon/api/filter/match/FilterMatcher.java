package cc.turtl.chiselmon.api.filter.match;

import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.Optional;

public class FilterMatcher {

    /**
     * Matches a Pokemon against all registered filters.
     * Returns a result containing the primary (highest priority) match and all applicable matches.
     */
    public static FilterMatchResult match(Pokemon pokemon) {
        List<RuntimeFilter> allFilters = FilterRegistry.getSorted();

        List<RuntimeFilter> matches = allFilters.stream()
                .filter(f -> f.condition().test(pokemon))
                .toList();

        // First match is primary since FilterRegistry.getSorted() should already be ordered by priority.
        Optional<RuntimeFilter> primary = matches.isEmpty()
                ? Optional.empty()
                : Optional.of(matches.getFirst());

        return new FilterMatchResult(
                pokemon,
                primary,
                matches
        );
    }
}