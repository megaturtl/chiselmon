package cc.turtl.chiselmon.client.system.dashboard

import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.BiomesHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.ContextHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.DimensionsHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.HeatmapHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.PlayerPosHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.RecentEncountersHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.SpeciesHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.StatsHandler
import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.TimelineHandler
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * Super basic and lightweight HTTP server for the Chiselmon data dashboard.
 *
 * Lifecycle:
 * ```
 * val server = DashboardServer(db, port)
 * server.start()
 * server.stop()
 * ```
 */
class DashboardServer(private val db: EncounterDatabase, private val port: Int) {

    private var httpServer: HttpServer? = null
    private val startTimeMs: Long = System.currentTimeMillis()

    fun start() {
        val server = HttpServer.create(InetSocketAddress(LOCALHOST, port), 0).apply {
            // Serves the HTML/CSS/JS for frontend
            createContext("/", StaticFileHandler())

            // Serves API endpoints which get data from the EncounterDatabase
            createContext("/api/dimensions/", DimensionsHandler(db))
            createContext("/api/stats/", StatsHandler(db))
            createContext("/api/species/", SpeciesHandler(db))
            createContext("/api/biomes/", BiomesHandler(db))
            createContext("/api/encounters/", RecentEncountersHandler(db))
            createContext("/api/timeline/", TimelineHandler(db))
            createContext("/api/heatmap/", HeatmapHandler(db))
            createContext("/api/context/", ContextHandler(db))
            createContext("/api/playerpos/", PlayerPosHandler(db))

            // Executes on a single thread for now, should be fine for a simple local dashboard
            executor = Executors.newSingleThreadExecutor { r ->
                Thread(r, "chiselmon-dashboard").apply { isDaemon = true }
            }
        }

        server.start()
        httpServer = server
        ChiselmonConstants.LOGGER.info("Chiselmon Dashboard started at http://localhost:{}/", port)
    }

    fun stop() {
        httpServer?.let {
            it.stop(0)
            ChiselmonConstants.LOGGER.info("Chiselmon Dashboard server stopped.")
        }
    }

    fun uptime(): Long = if (httpServer != null) System.currentTimeMillis() - startTimeMs else 0

    companion object {
        private const val LOCALHOST = "127.0.0.1"
    }
}
