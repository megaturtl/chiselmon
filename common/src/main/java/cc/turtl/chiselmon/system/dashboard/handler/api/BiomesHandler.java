package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BiomesHandler extends ApiHandler {

    public BiomesHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<String> entries = new ArrayList<>();

            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT biome, COUNT(*) as count FROM encounters GROUP BY biome ORDER BY count DESC LIMIT 15");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(String.format("{\"biome\":\"%s\",\"count\":%d}",
                            escape(rs.getString("biome")), rs.getLong("count")));
                }
            }

            sendJson(exchange, 200, "[" + String.join(",", entries) + "]");

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }
}
