package cc.turtl.chiselmon.system.dashboard.api.endpoints

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class PlayerPosHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class PlayerPositionResponse(val lastX: Int, val lastZ: Int, val lastDimension: String)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, _ ->
            query("encounters").timeRange(timeRange)
                .select("player_x, player_z, dimension")
                .orderBy("encountered_ms DESC")
                .limit(1)
                .fetchOne { rs ->
                    PlayerPositionResponse(
                        lastX = rs.getInt("player_x"),
                        lastZ = rs.getInt("player_z"),
                        lastDimension = rs.getString("dimension"),
                    )
                } ?: DEFAULT_POSITION
        }
    }

    companion object {
        private val DEFAULT_POSITION = PlayerPositionResponse(0, 0, "minecraft:overworld")
    }
}
