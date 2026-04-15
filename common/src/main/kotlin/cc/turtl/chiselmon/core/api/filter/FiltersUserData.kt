package cc.turtl.chiselmon.core.api.filter

import cc.turtl.chiselmon.core.api.filter.match.FilterMatcher

/**
 * Persistent storage for user-defined filter definitions.
 * Registered globally as the single source of truth for filters.
 */
class FiltersUserData {
    private val filters: LinkedHashMap<String, FilterDefinition> = LinkedHashMap()

    val all: Map<String, FilterDefinition>
        get() = filters.toMap()

    fun migrateAll() {
        FilterDefinition.DefaultFilters.all().values.forEach { def ->
            filters.putIfAbsent(def.id, def)
        }
    }

    fun put(id: String, def: FilterDefinition) {
        filters[id] = def
        FilterMatcher.invalidateCache()
    }

    fun remove(id: String) {
        filters.remove(id)
        FilterMatcher.invalidateCache()
    }

    companion object {
        @JvmStatic
        fun withDefaults(): FiltersUserData = FiltersUserData().also { it.migrateAll() }
    }
}
