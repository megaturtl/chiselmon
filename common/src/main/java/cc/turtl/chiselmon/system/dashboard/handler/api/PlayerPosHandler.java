package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerPosHandler extends ApiHandler {

    public PlayerPosHandler(EncounterDatabase db) {
        super(db);
    }

    public void handle(HttpExchange exchange) throws IOException {
        // Last recorded player position + dimension
        int lastX = 0, lastZ = 0;
        String lastDimension = "minecraft:overworld";
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT player_x, player_z, dimension FROM encounters ORDER BY encountered_ms DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lastX = rs.getInt("player_x");
                lastZ = rs.getInt("player_z");
                lastDimension = rs.getString("dimension");
            }
        } catch (SQLException ignored) {
        }

        sendJson(exchange, 200, String.format(
                "{\"lastX\":%d,\"lastZ\":%d,\"lastDimension\":\"%s\"}",
                lastX, lastZ, escape(lastDimension)));
    }
}
