package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.filter.FilterDefinition;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import java.util.function.Predicate;

public class FilterRegistry {
    private static final Map<String, RuntimeFilter> FILTERS = new LinkedHashMap<>();

    /**
     * Reloads all filters from config definitions.
     */
    public static void loadFromConfig() {
        FILTERS.clear();

        Map<String, FilterDefinition> definitions = ChiselmonConfig.get().filter.filters;

        // Load all filters regardless of enabled status
        definitions.values().forEach(FilterRegistry::register);
    }

    /**
     * Converts a definition to a runtime filter and registers it.
     */
    private static void register(FilterDefinition def) {
        // Combine all tag predicates with AND logic
        Predicate<Pokemon> condition = def.tags.stream()
                .map(FilterTagParser::parse)
                .reduce(Predicate::and)
                .orElse(p -> true);

        RuntimeFilter filter = new RuntimeFilter(
                def.id,
                def.color,
                def.priority,
                condition
        );

        FILTERS.put(def.id, filter);
    }

    /**
     * Returns a list of all registered filters.
     */
    public static List<RuntimeFilter> getSorted() {
        loadFromConfig();
        return List.copyOf(FILTERS.values());
    }

    /**
     * Retrieves a specific filter by ID.
     */
    public static Optional<RuntimeFilter> get(String id) {
        loadFromConfig();
        return Optional.ofNullable(FILTERS.get(id));
    }
}