package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.filter.match.FilterMatcher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent storage for user-defined filter definitions.
 * Registered globally as the single source of truth for filters.
 */
public class FiltersUserData {
    private final Map<String, FilterDefinition> filters = new LinkedHashMap<>();

    public static FiltersUserData withDefaults() {
        FiltersUserData data = new FiltersUserData();
        for (FilterDefinition def : FilterDefinition.DefaultFilters.all().values()) {
            data.filters.putIfAbsent(def.id, def);
        }
        return data;
    }

    /**
     * Migrates any definitions still using the old {@code List<String> tags} format to
     * {@code conditionString}, and backfills any built-in defaults added since the last save.
     * Safe to call on every cache rebuild.
     */
    public void migrateAll() {
        for (FilterDefinition def : filters.values()) {
            def.migrateIfNeeded();
        }
        for (FilterDefinition def : FilterDefinition.DefaultFilters.all().values()) {
            filters.putIfAbsent(def.id, def);
        }
    }

    public Map<String, FilterDefinition> getAll() {
        return Collections.unmodifiableMap(filters);
    }

    public void put(String id, FilterDefinition def) {
        filters.put(id, def);
        FilterMatcher.invalidateCache();
    }

    public void remove(String id) {
        filters.remove(id);
        FilterMatcher.invalidateCache();
    }

    public boolean has(String id) {
        return filters.containsKey(id);
    }
}