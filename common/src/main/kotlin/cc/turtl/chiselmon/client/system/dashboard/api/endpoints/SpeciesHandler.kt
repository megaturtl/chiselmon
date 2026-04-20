package cc.turtl.chiselmon.client.system.dashboard.api.endpoints

import cc.turtl.chiselmon.client.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class SpeciesHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class SpeciesEntry(val species: String, val count: Long)
    private data class SpeciesResponse(val species: List<SpeciesEntry>)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, params ->
            val limit = parseIntParam(params, "limit", DEFAULT_LIMIT)

            val species = query("encounters")
                .timeRange(timeRange)
                .select("species, COUNT(*) as cnt")
                .groupBy("species")
                .orderBy("cnt DESC")
                .limit(limit)
                .fetchList { rs -> SpeciesEntry(rs.getString("species"), rs.getLong("cnt")) }

            SpeciesResponse(species)
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 20
    }
}
