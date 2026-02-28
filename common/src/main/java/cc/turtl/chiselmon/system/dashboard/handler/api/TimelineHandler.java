package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimelineHandler extends ApiHandler {

    public TimelineHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            long from = parseFrom(exchange);

            // Parse ?granularity=minute|hour (default: hour)
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String granularity = params.getOrDefault("granularity", "hour");
            boolean isMinute = "minute".equalsIgnoreCase(granularity);

            // Bucket size in milliseconds
            long bucketMs = isMinute ? 60_000L : 3_600_000L;

            List<String> entries = new ArrayList<>();

            String sql = "SELECT FLOOR(encountered_ms / " + bucketMs + ") * " + bucketMs + " AS bucket,"
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