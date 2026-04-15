package cc.turtl.chiselmon.system.dashboard.api.endpoints

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class BiomesHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class BiomeEntry(val biome: String, val count: Long)
    private data class BiomesResponse(val biomes: List<BiomeEntry>)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, params ->
            val limit = parseIntParam(params, "limit", DEFAULT_LIMIT)

            val biomes = query("encounters")
                .timeRange(timeRange)
                .select("biome, COUNT(*) as cnt")
                .groupBy("biome")
                .orderBy("cnt DESC")
                .limit(limit)
                .fetchList { rs -> BiomeEntry(rs.getString("biome"), rs.getLong("cnt")) }

            BiomesResponse(biomes)
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 15
    }
}
