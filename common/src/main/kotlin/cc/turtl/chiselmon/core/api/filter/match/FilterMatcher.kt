package cc.turtl.chiselmon.core.api.filter.match

import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.filter.FilterConditionParser
import cc.turtl.chiselmon.core.api.filter.RuntimeFilter
import cc.turtl.chiselmon.core.api.storage.Scope
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.function.Predicate

object FilterMatcher {
    private var cache: List<RuntimeFilter>? = null

    @JvmStatic
    fun match(pokemon: Pokemon): FilterMatchResult {
        val filters = getFilters()
        val matches = filters.filter { it.condition.test(pokemon) }
        val primary = matches.maxByOrNull { it.priority }
        return FilterMatchResult(pokemon, primary, matches)
    }

    @JvmStatic
    fun invalidateCache() {
        cache = null
    }

    private fun getFilters(): List<RuntimeFilter> {
        return cache ?: createRuntimeFilters().also { cache = it }
    }

    private fun createRuntimeFilters(): List<RuntimeFilter> {
        val data = ChiselmonStorage.FILTERS[Scope.global()]
        data.migrateAll()

        return data.all.values
            .map { def ->
                val condition: Predicate<Pokemon> = try {
                    FilterConditionParser.parse(def.conditionString).toPredicate()
                } catch (e: Exception) {
                    ChiselmonConstants.LOGGER.warn(
                        "Filter '{}' has an invalid condition '{}': {}",
                        def.id,
                        def.conditionString,
                        e.message
                    )
                    Predicate { false }
                }
                RuntimeFilter(def.id, def.displayName, def.rgb, def.priority, condition)
            }
            .sortedByDescending { it.priority }
    }
}