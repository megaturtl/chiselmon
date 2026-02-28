package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecentEncountersHandler extends ApiHandler {

    public RecentEncountersHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            long from = parseFrom(exchange);
            List<String> entries = new ArrayList<>();

            String sql = "SELECT species, form, level, gender, scale_modifier, is_shiny, is_legendary, from_snack,"
                    + " block_name, dimension, biome, encountered_ms FROM encounters"
                    + (from > 0 ? " WHERE encountered_ms >= " + from : "")
                    + " ORDER BY encountered_ms DESC LIMIT 50";

            try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(String.format(
                            "{\"species\":\"%s\",\"form\":\"%s\",\"level\":%d,\"gender\":\"%s\",\"scale\":%f," +
                                    "\"shiny\":%b,\"legendary\":%b,\"snack\":%b,\"dimension\":\"%s\",\"block_name\":\"%s\",\"biome\":\"%s\",\"ms\":%d}",
                            escape(rs.getString("species")),
                            escape(rs.getString("form")),
                            rs.getInt("level"),
                            escape(rs.getString("gender")),
                            rs.getDouble("scale_modifier"),
                            rs.getBoolean("is_shiny"),
                            rs.getBoolean("is_legendary"),
                            rs.getBoolean("from_snack"),
                            escape(rs.getString("dimension")),
                            escape(rs.getString("block_name")),
                            escape(rs.getString("biome")),
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