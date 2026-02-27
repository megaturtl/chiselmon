package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecentShiniesHandler extends ApiHandler {

    public RecentShiniesHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<String> entries = new ArrayList<>();

            String sql = """
                        SELECT species, form, level, biome, dimension, encountered_ms
                        FROM encounters
                        WHERE is_shiny = TRUE
                        ORDER BY encountered_ms DESC
                        LIMIT 100
                    """;
            try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(String.format(
                            "{\"species\":\"%s\",\"form\":\"%s\",\"level\":%d,\"biome\":\"%s\",\"dimension\":\"%s\",\"ms\":%d}",
                            escape(rs.getString("species")),
                            escape(rs.getString("form")),
                            rs.getInt("level"),
                            escape(rs.getString("biome")),
                            escape(rs.getString("dimension")),
                            rs.getLong("encountered_ms")
                    ));
                }
            }

            sendJson(exchange, 200, "[" + String.join(",", entries) + "]");

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }
}
