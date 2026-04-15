package cc.turtl.chiselmon.system.dashboard.api.endpoints

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

/**
 * Aggregated top-level statistics for the encounters table.
 *
 * Also returns [StatsResponse.activeMinutes]: the number of distinct 1-minute buckets that
 * contain at least one encounter. The frontend uses this to calculate spawns/min, excluding
 * offline and AFK gaps.
 */
class StatsHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class StatsResponse(
        val totalEncounters: Long,
        val shinies: Long,
        val legendaries: Long,
        val sizeVariations: Long,
        val uniqueSpecies: Long,
        val dimensions: Long,
        val snackSpawns: Long,
        val activeMinutes: Long,
    )

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, _ ->
            StatsResponse(
                totalEncounters = query("encounters").timeRange(timeRange).fetchCount(),
                shinies = query("encounters").timeRange(timeRange).where("is_shiny = TRUE").fetchCount(),
                legendaries = query("encounters").timeRange(timeRange).where("is_legendary = TRUE").fetchCount(),
                sizeVariations = query("encounters").timeRange(timeRange).where("scale_modifier != 1.0").fetchCount(),
                uniqueSpecies = query("encounters").timeRange(timeRange)
                    .select("COUNT(DISTINCT species)").fetchOne { it.getLong(1) } ?: 0L,
                dimensions = query("encounters").timeRange(timeRange)
                    .select("COUNT(DISTINCT dimension)").fetchOne { it.getLong(1) } ?: 0L,
                snackSpawns = query("encounters").timeRange(timeRange).where("from_snack = TRUE").fetchCount(),
                activeMinutes = query("encounters").timeRange(timeRange)
                    .select("COUNT(DISTINCT FLOOR(encountered_ms / 60000))").fetchOne { it.getLong(1) } ?: 0L,
            )
        }
    }
}
