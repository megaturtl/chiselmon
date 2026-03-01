package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ContextHandler extends ApiHandler {

    private record ContextResponse(String worldType, String worldName, String modVersion) {
    }

    public ContextHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            String worldType, worldName;
            String dbFolder = db.getDbPath().getParent().getFileName().toString();

            if (dbFolder.startsWith("mp-")) {
                worldType = "mp";
                worldName = dbFolder.substring(3);
            } else if (dbFolder.startsWith("sp-")) {
                worldType = "sp";
                worldName = dbFolder.substring(3).replace('_', ' ');
            } else {
                worldType = "unknown";
                worldName = "unknown";
            }

            return new ContextResponse(worldType, worldName, ChiselmonConstants.VERSION);
        });
    }
}