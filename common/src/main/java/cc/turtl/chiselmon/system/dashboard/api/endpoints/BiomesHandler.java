package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class BiomesHandler extends ApiHandler {

    private static final int DEFAULT_LIMIT = 15;

    private record BiomeEntry(String biome, long count) {
    }

    private record BiomesResponse(List<BiomeEntry> biomes) {
    }

    public BiomesHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            int limit = parseIntParam(params, "limit", DEFAULT_LIMIT);

            List<BiomeEntry> biomes = query("encounters")
                    .timeRange(timeRange)
                    .select("biome, COUNT(*) as cnt")
                    .groupBy("biome")
                    .orderBy("cnt DESC")
                    .limit(limit)
                    .fetchList(rs -> new BiomeEntry(rs.getString("biome"), rs.getLong("cnt")));

            return new BiomesResponse(biomes);
        });
    }
}