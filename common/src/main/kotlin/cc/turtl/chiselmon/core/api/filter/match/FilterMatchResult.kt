package cc.turtl.chiselmon.core.api.filter.match

import cc.turtl.chiselmon.core.api.filter.RuntimeFilter
import com.cobblemon.mod.common.pokemon.Pokemon

data class FilterMatchResult(
    @JvmField val pokemon: Pokemon,
    @JvmField val primaryMatch: RuntimeFilter?,
    @JvmField val allMatches: List<RuntimeFilter>
)