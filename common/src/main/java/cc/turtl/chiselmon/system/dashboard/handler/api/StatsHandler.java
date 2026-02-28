package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Basic top-level stat overview of encounters
 */
public class StatsHandler extends ApiHandler {

    public StatsHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            long from = parseFrom(exchange);
            Connection conn = db.getConnection();
            long total, shinies, legendaries, size_variations, species, dimensions, snackSpawns;

            try (Statement s = conn.createStatement()) {
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(*) FROM encounters", from))) {
                    total = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(*) FROM encounters WHERE is_shiny = TRUE", from))) {
                    shinies = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(*) FROM encounters WHERE is_legendary = TRUE", from))) {
                    legendaries = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(*) FROM encounters WHERE scale_modifier != 1.0", from))) {
                    size_variations = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(DISTINCT species) FROM encounters", from))) {
                    species = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(DISTINCT dimension) FROM encounters", from))) {
                    dimensions = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery(where("SELECT COUNT(*) FROM encounters WHERE from_snack = TRUE", from))) {
                    snackSpawns = rs.next() ? rs.getLong(1) : 0;
                }
            }

            sendJson(exchange, 200, String.format(
                    "{\"total\":%d,\"shinies\":%d,\"legendaries\":%d,\"size_variations\":%d,\"uniqueSpecies\":%d,\"dimensions\":%d,\"snackSpawns\":%d}",
                    total, shinies, legendaries, size_variations, species, dimensions, snackSpawns
            ));

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }

    /**
     * Appends encountered_ms >= from to a query.
     * Handles queries that already have a WHERE clause.
     * from=0 means all-time, so no filter appended.
     */
    private static String where(String sql, long from) {
        if (from <= 0) return sql;
        if (sql.toUpperCase().contains("WHERE")) {
            return sql + " AND encountered_ms >= " + from;
        }
        return sql + " WHERE encountered_ms >= " + from;
    }
}