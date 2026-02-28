package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimelineHandler extends ApiHandler {

    public TimelineHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            long from = parseFrom(exchange);
            List<String> entries = new ArrayList<>();

            String sql = "SELECT FLOOR(encountered_ms / 3600000) * 3600000 AS bucket,"
                    + " COUNT(*) AS cnt FROM encounters"
                    + (from > 0 ? " WHERE encountered_ms >= " + from : "")
                    + " GROUP BY bucket ORDER BY bucket ASC";

            try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(String.format("{\"bucket\":%d,\"count\":%d}",
                            rs.getLong("bucket"), rs.getLong("cnt")));
                }
            }

            sendJson(exchange, 200, "[" + String.join(",", entries) + "]");

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }
}