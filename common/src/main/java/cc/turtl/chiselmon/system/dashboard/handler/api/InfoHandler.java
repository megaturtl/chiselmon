package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InfoHandler extends ApiHandler {

    public InfoHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String folder = db.getDbPath().getParent().getFileName().toString();

        String type, name;
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

        // All known dimensions ordered by encounter count
        List<String> dimensions = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(
                "SELECT dimension FROM encounters GROUP BY dimension ORDER BY COUNT(*) DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dimensions.add("\"" + escape(rs.getString("dimension")) + "\"");
            }
        } catch (SQLException ignored) {
        }

        sendJson(exchange, 200, String.format(
                "{\"type\":\"%s\",\"name\":\"%s\",\"lastX\":%d,\"lastZ\":%d,\"lastDimension\":\"%s\",\"dimensions\":[%s]}",
                escape(type), escape(name), lastX, lastZ, escape(lastDimension), String.join(",", dimensions)
        ));
    }
}