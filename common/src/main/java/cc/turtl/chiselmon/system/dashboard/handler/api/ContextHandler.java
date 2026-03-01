package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ContextHandler extends ApiHandler {

    public ContextHandler(EncounterDatabase db) {
        super(db);
    }

    public void handle(HttpExchange exchange) throws IOException {
        String modVersion = ChiselmonConstants.VERSION;
        String worldType, worldName; // multiplayer or singleplayer and the world identifier

        // find the world id by checking db path
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

        sendJson(exchange, 200, String.format(
                "{\"worldType\":\"%s\",\"worldName\":\"%s\",\"modVersion\":\"%s\"}",
                escape(worldType), escape(worldName), escape(modVersion)));
    }
}
