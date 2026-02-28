package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DimensionsHandler extends ApiHandler {

    public DimensionsHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<String> entries = new ArrayList<>();

            try (PreparedStatement ps = db.getConnection().prepareStatement(
                    "SELECT dimension, COUNT(*) as cnt FROM encounters GROUP BY dimension ORDER BY cnt DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(String.format("{\"dimension\":\"%s\",\"count\":%d}",
                            escape(rs.getString("dimension")), rs.getLong("cnt")));
                }
            }

            sendJson(exchange, 200, "[" + String.join(",", entries) + "]");

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }
}