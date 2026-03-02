package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class SpeciesHandler extends ApiHandler {

    private static final int DEFAULT_LIMIT = 20;

    private record SpeciesEntry(String species, long count) {
    }

    private record SpeciesResponse(List<SpeciesEntry> species) {
    }

    public SpeciesHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            int limit = parseIntParam(params, "limit", DEFAULT_LIMIT);

            List<SpeciesEntry> species = query("encounters")
                    .timeRange(timeRange)
                    .select("species, COUNT(*) as cnt")
                    .groupBy("species")
                    .orderBy("cnt DESC")
                    .limit(limit)
                    .fetchList(rs -> new SpeciesEntry(rs.getString("species"), rs.getLong("cnt")));

            return new SpeciesResponse(species);
        });
    }
}