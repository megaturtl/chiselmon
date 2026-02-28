package cc.turtl.chiselmon.system.dashboard;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.dashboard.handler.StaticFileHandler;
import cc.turtl.chiselmon.system.dashboard.handler.api.*;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Super basic and lightweight HTTP server for the Chiselmon data dashboard.
 * <p>
 * Lifecycle:
 * <code>
 * <br>DashboardServer server = new DashboardServer(db, port);
 * <br>server.start();
 * <br>server.stop();
 * </code>
 */
public class DashboardServer {
    private static final String LOCALHOST = "127.0.0.1";

    private final EncounterDatabase db;
    private final int port;
    private HttpServer httpServer;
    private final long startTimeMs;

    public DashboardServer(EncounterDatabase db, int port) {
        this.db = db;
        this.port = port;
        this.startTimeMs = System.currentTimeMillis();
    }

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(LOCALHOST, port), 0);

        // Serves the HTML/CSS/JS for frontend
        httpServer.createContext("/", new StaticFileHandler());

        // Serves API endpoints which get data from the EncounterDatabase
        httpServer.createContext("/api/info", new InfoHandler(db));
        httpServer.createContext("/api/dimensions", new DimensionsHandler(db));
        httpServer.createContext("/api/stats", new StatsHandler(db));
        httpServer.createContext("/api/species", new SpeciesHandler(db));
        httpServer.createContext("/api/biomes", new BiomesHandler(db));
        httpServer.createContext("/api/encounters", new RecentEncountersHandler(db));
        httpServer.createContext("/api/timeline", new TimelineHandler(db));
        httpServer.createContext("/api/heatmap", new HeatmapHandler(db));

        // Executes on a single thread for now, should be fine for a simple local dashboard
        httpServer.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "chiselmon-dashboard");
            t.setDaemon(true);
            return t;
        }));

        httpServer.start();
        ChiselmonConstants.LOGGER.info("Chiselmon Dashboard started at http://localhost:{}/", port);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            ChiselmonConstants.LOGGER.info("Chiselmon Dashboard server stopped.");
        }
    }

    public long uptime() {
        if (httpServer != null) {
            return System.currentTimeMillis() - startTimeMs;
        }
        return 0;
    }
}