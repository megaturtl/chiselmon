package cc.turtl.chiselmon.client.system.dashboard

import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.*
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import cc.turtl.chiselmon.core.ChiselmonConstants
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

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
class DashboardServer(
    private val db: EncounterDatabase,
    private val port: Int,
) {
    private var httpServer: HttpServer? = null
    private var startTimeMs: Long = 0

    fun start() {
        // The native single-threaded dispatcher lets stop() join handlers before database disposal.
        val server =
            HttpServer.create(InetSocketAddress(LOCALHOST, port), 0).apply {
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
            }

        server.start()
        httpServer = server
        startTimeMs = System.currentTimeMillis()
        ChiselmonConstants.LOGGER.info("Chiselmon Dashboard started at http://localhost:{}/", port)
    }

    fun stop() {
        val server = httpServer ?: return
        server.stop(0)
        check(!Thread.currentThread().isInterrupted) { "Dashboard shutdown interrupted; database must remain open" }
        httpServer = null
        ChiselmonConstants.LOGGER.info("Chiselmon Dashboard server stopped.")
    }

    fun uptime(): Long = if (httpServer != null) System.currentTimeMillis() - startTimeMs else 0

    companion object {
        private const val LOCALHOST = "127.0.0.1"
    }
}
