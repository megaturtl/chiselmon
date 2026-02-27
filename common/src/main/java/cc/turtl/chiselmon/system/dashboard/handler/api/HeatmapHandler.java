package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatmapHandler extends ApiHandler {

    public HeatmapHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());

        int cx, cz, radius;
        try {
            cx = Integer.parseInt(params.getOrDefault("cx", "0"));
            cz = Integer.parseInt(params.getOrDefault("cz", "0"));
            radius = Integer.parseInt(params.getOrDefault("radius", "256"));
        } catch (NumberFormatException e) {
            sendError(exchange, "cx, cz, and radius must be integers");
            return;
        }

        radius = Math.max(16, Math.min(radius, 4096));   // clamp: 16 – 4096

        try {
            List<String> pokemon = new ArrayList<>();
            List<String> player = new ArrayList<>();

            String sql = """
                        SELECT pokemon_x, pokemon_z, player_x, player_z
                        FROM encounters
                        WHERE pokemon_x BETWEEN ? AND ?
                          AND pokemon_z BETWEEN ? AND ?
                    """;

            try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                ps.setInt(1, cx - radius);
                ps.setInt(2, cx + radius);
                ps.setInt(3, cz - radius);
                ps.setInt(4, cz + radius);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pokemon.add(String.format("[%d,%d]", rs.getInt("pokemon_x"), rs.getInt("pokemon_z")));
                        player.add(String.format("[%d,%d]", rs.getInt("player_x"), rs.getInt("player_z")));
                    }
                }
            }

            String json = String.format(
                    "{\"cx\":%d,\"cz\":%d,\"radius\":%d,\"pokemon\":[%s],\"player\":[%s]}",
                    cx, cz, radius,
                    String.join(",", pokemon),
                    String.join(",", player)
            );
            sendJson(exchange, 200, json);

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}