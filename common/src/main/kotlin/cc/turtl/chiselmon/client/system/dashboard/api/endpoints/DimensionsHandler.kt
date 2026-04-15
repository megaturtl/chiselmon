package cc.turtl.chiselmon.client.system.dashboard.api.endpoints

import cc.turtl.chiselmon.client.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class DimensionsHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class DimensionEntry(val dimension: String, val count: Long)
    private data class DimensionsResponse(val dimensions: List<DimensionEntry>)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, _ ->
            val dimensions = query("encounters")
                .timeRange(timeRange)
                .select("dimension, COUNT(*) as cnt")
                .groupBy("dimension")
                .orderBy("cnt DESC")
                .fetchList { rs -> DimensionEntry(rs.getString("dimension"), rs.getLong("cnt")) }
            DimensionsResponse(dimensions)
        }
    }
}
