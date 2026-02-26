package cc.turtl.chiselmon.api.filter.match;

import cc.turtl.chiselmon.api.filter.FilterConditionParser;
import cc.turtl.chiselmon.api.filter.FiltersUserData;
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

    private static List<RuntimeFilter> createRuntimeFilters() {
        FiltersUserData data = ChiselmonStorage.FILTERS.get(StorageScope.global());
        data.migrateAll();

        return data.getAll().values().stream()
                .map(def -> {
                    Predicate<Pokemon> condition;
                    try {
                        condition = FilterConditionParser.parse(def.conditionString).toPredicate();
                    } catch (Exception e) {
                        condition = p -> false;
                    }
                    return new RuntimeFilter(def.id, def.displayName, def.rgb, def.priority, condition);
                })
                .sorted(Comparator.comparing(RuntimeFilter::priority).reversed())
                .toList();
    }
}