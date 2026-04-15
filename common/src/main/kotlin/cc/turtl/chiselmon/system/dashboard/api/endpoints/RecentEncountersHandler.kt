package cc.turtl.chiselmon.system.dashboard.api.endpoints

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class RecentEncountersHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class EncounterEntry(
        val species: String,
        val form: String,
        val level: Int,
        val gender: String,
        val scale: Double,
        val shiny: Boolean,
        val legendary: Boolean,
        val fromSnack: Boolean,
        val dimension: String,
        val blockName: String,
        val biome: String,
        val ms: Long,
    )

    private data class EncounterResponse(val encounters: List<EncounterEntry>)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, params ->
            val limit = parseIntParam(params, "limit", DEFAULT_LIMIT).coerceAtMost(MAX_LIMIT)

            val encounters = query("encounters").timeRange(timeRange)
                .select(
                    "species, form, level, gender, scale_modifier, is_shiny, is_legendary," +
                        " from_snack, block_name, dimension, biome, encountered_ms"
                )
                .orderBy("encountered_ms DESC")
                .limit(limit)
                .fetchList { rs ->
                    EncounterEntry(
                        species = rs.getString("species"),
                        form = rs.getString("form"),
                        level = rs.getInt("level"),
                        gender = rs.getString("gender"),
                        scale = rs.getDouble("scale_modifier"),
                        shiny = rs.getBoolean("is_shiny"),
                        legendary = rs.getBoolean("is_legendary"),
                        fromSnack = rs.getBoolean("from_snack"),
                        dimension = rs.getString("dimension"),
                        blockName = rs.getString("block_name"),
                        biome = rs.getString("biome"),
                        ms = rs.getLong("encountered_ms"),
                    )
                }

            EncounterResponse(encounters)
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 50
        private const val MAX_LIMIT = 500
    }
}
