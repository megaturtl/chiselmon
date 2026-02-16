package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import java.util.*;
import java.util.function.Predicate;

/**
 * FilterRegistry is the single source of truth for runtime filters.
 * It loads filter definitions from config and converts them to runtime filters.
 * Call loadFromConfig() once when config changes, then use get/getSorted to access filters.
 */
public class FilterRegistry {
    private static final Map<String, RuntimeFilter> FILTERS = new LinkedHashMap<>();
    private static boolean loaded = false;

    /**
     * Reloads all filters from config definitions.
     * This should be called:
     * - On mod initialization
     * - After config save (when filters are added/removed/modified)
     */
    public static void loadFromConfig() {
        FILTERS.clear();
        loaded = false;

        Map<String, FilterDefinition> definitions = ChiselmonConfig.get().filter.filters;

        definitions.values().forEach(FilterRegistry::register);
        
        loaded = true;
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
                def.color.getRGB(),
                def.priority,
                condition
        );

        FILTERS.put(def.id, filter);
    }

    /**
     * Returns a list of all registered filters.
     * Ensures filters are loaded from config.
     */
    public static List<RuntimeFilter> getSorted() {
        ensureLoaded();
        return List.copyOf(FILTERS.values());
    }

    /**
     * Retrieves a specific filter by ID.
     * Ensures filters are loaded from config.
     */
    public static Optional<RuntimeFilter> get(String id) {
        ensureLoaded();
        return Optional.ofNullable(FILTERS.get(id));
    }

    /**
     * Ensures filters are loaded. Loads automatically if not yet loaded.
     * This makes the registry safe to use without explicit initialization.
     */
    private static void ensureLoaded() {
        if (!loaded) {
            loadFromConfig();
        }
    }

    /**
     * Clears all filters. Used for testing or cleanup.
     */
    public static void clear() {
        FILTERS.clear();
        loaded = false;
    }
}