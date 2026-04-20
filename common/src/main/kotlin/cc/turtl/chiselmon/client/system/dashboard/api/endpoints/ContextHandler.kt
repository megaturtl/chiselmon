package cc.turtl.chiselmon.client.system.dashboard.api.endpoints

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.client.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class ContextHandler(db: EncounterDatabase) : ApiHandler(db) {

    private data class ContextResponse(val worldType: String, val worldName: String, val modVersion: String)

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { _, _ ->
            val dbFolder = db.dbPath.parent.fileName.toString()
            val (worldType, worldName) = when {
                dbFolder.startsWith("mp-") -> "mp" to dbFolder.substring(3)
                dbFolder.startsWith("sp-") -> "sp" to dbFolder.substring(3).replace('_', ' ')
                else -> "unknown" to "unknown"
            }
            ContextResponse(worldType, worldName, BuildDetails.MOD_VERSION)
        }
    }
}
