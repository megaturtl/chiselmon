package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class DimensionsHandler extends ApiHandler {

    private record DimensionEntry(String dimension, long count) {
    }

    private record DimensionsResponse(List<DimensionEntry> dimensions) {
    }

    public DimensionsHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            List<DimensionEntry> dimensions = query("encounters")
                    .timeRange(timeRange)
                    .select("dimension, COUNT(*) as cnt")
                    .groupBy("dimension")
                    .orderBy("cnt DESC")
                    .fetchList(rs -> new DimensionEntry(rs.getString("dimension"), rs.getLong("cnt")));

            return new DimensionsResponse(dimensions);
        });
    }
}