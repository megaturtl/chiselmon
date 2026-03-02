package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Aggregated top-level statistics for the encounters table.
 * <p>
 * Also returns {@code activeMinutes}: the number of distinct 1-minute buckets
 * that contain at least one encounter. The frontend uses this to calculate
 * spawns/min, excluding offline and AFK gaps.
 */
public class StatsHandler extends ApiHandler {

    private record StatsResponse(
            long totalEncounters,
            long shinies,
            long legendaries,
            long sizeVariations,
            long uniqueSpecies,
            long dimensions,
            long snackSpawns,
            long activeMinutes
    ) {
    }

    public StatsHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> new StatsResponse(
                query("encounters").timeRange(timeRange).fetchCount(),
                query("encounters").timeRange(timeRange).where("is_shiny = TRUE").fetchCount(),
                query("encounters").timeRange(timeRange).where("is_legendary = TRUE").fetchCount(),
                query("encounters").timeRange(timeRange).where("scale_modifier != 1.0").fetchCount(),
                query("encounters").timeRange(timeRange).select("COUNT(DISTINCT species)").fetchOne(rs -> rs.getLong(1)),
                query("encounters").timeRange(timeRange).select("COUNT(DISTINCT dimension)").fetchOne(rs -> rs.getLong(1)),
                query("encounters").timeRange(timeRange).where("from_snack = TRUE").fetchCount(),
                query("encounters").timeRange(timeRange).select("COUNT(DISTINCT FLOOR(encountered_ms / 60000))").fetchOne(rs -> rs.getLong(1))
        ));
    }
}