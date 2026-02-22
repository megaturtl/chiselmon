package cc.turtl.chiselmon.api.filter.match;

import cc.turtl.chiselmon.api.filter.FilterTagParser;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.ChiselmonStorage;
import cc.turtl.chiselmon.api.storage.StorageScope;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class FilterMatcher {
    private static List<RuntimeFilter> CACHE = null;

    public static FilterMatchResult match(Pokemon pokemon) {
        List<RuntimeFilter> filters = getFilters();

        List<RuntimeFilter> matches = filters.stream()
                .filter(f -> f.condition().test(pokemon))
                .toList();

        Optional<RuntimeFilter> primary = matches.stream()
                .max(Comparator.comparing(RuntimeFilter::priority));

        return new FilterMatchResult(pokemon, primary, matches);
    }

    public static void invalidateCache() {
        CACHE = null;
    }

    private static List<RuntimeFilter> getFilters() {
        if (CACHE == null) {
            CACHE = createRuntimeFilters();
        }
        return CACHE;
    }

    // Compiles filter definitions into sorted runtime filters with their predicates.
    private static List<RuntimeFilter> createRuntimeFilters() {
        return ChiselmonStorage.FILTERS.get(StorageScope.global()).getAll().values().stream()
                .map(def -> {
                    Predicate<Pokemon> condition = def.tags.stream()
                            .map(FilterTagParser::parse)
                            .reduce(Predicate::and)
                            .orElse(p -> true);

                    return new RuntimeFilter(def.id, def.displayName, def.rgb, def.priority, condition);
                })
                .sorted(Comparator.comparing(RuntimeFilter::priority).reversed())
                .toList();
    }
}