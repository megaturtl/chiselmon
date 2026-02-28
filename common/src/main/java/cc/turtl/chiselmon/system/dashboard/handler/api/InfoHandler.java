package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;

public class InfoHandler extends ApiHandler {

    public InfoHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // I could use the static StorageScope.currentScope()
        // but maybe down the line I'll let users explore other servers/worlds, not just currently open one
        String folder = db.getDbPath().getParent().getFileName().toString();

        // Folder format: mp-<address> or sp-<world_name>
        // Strip the prefix and replace underscores for display
        String type;
        String name;
        if (folder.startsWith("mp-")) {
            type = "mp";
            name = folder.substring(3);
        } else if (folder.startsWith("sp-")) {
            type = "sp";
            name = folder.substring(3).replace('_', ' ');
        } else {
            type = "unknown";
            name = folder;
        }

        // Last recorded player position (used to centre the heatmap)
        int lastX = 0, lastZ = 0;
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT player_x, player_z FROM encounters ORDER BY encountered_ms DESC LIMIT 1");
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lastX = rs.getInt("player_x");
                lastZ = rs.getInt("player_z");
            }
        } catch (java.sql.SQLException ignored) {
        }

        sendJson(exchange, 200, String.format(
                "{\"type\":\"%s\",\"name\":\"%s\",\"lastX\":%d,\"lastZ\":%d}",
                escape(type), escape(name), lastX, lastZ
        ));
    }
}