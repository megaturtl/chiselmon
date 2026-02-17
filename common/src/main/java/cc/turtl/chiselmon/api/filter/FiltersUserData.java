package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.filter.match.FilterMatcher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persistent storage for user-defined filter definitions.
 * Registered globally - this is the single source of truth for filters.
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