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
            Connection conn = db.getConnection();
            long total, shinies, legendaries, species, snackSpawns;

            try (Statement s = conn.createStatement()) {
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM encounters")) {
                    total = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM encounters WHERE is_shiny = TRUE")) {
                    shinies = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM encounters WHERE is_legendary = TRUE")) {
                    legendaries = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(DISTINCT species) FROM encounters")) {
                    species = rs.next() ? rs.getLong(1) : 0;
                }
                try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM encounters WHERE from_snack = TRUE")) {
                    snackSpawns = rs.next() ? rs.getLong(1) : 0;
                }
            }

            sendJson(exchange, 200, String.format(
                    "{\"total\":%d,\"shinies\":%d,\"legendaries\":%d,\"uniqueSpecies\":%d,\"snackSpawns\":%d}",
                    total, shinies, legendaries, species, snackSpawns
            ));

        } catch (SQLException e) {
            sendError(exchange, e.getMessage());
        }
    }
}