package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class RecentEncountersHandler extends ApiHandler {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 500;

    private record EncounterEntry(
            String species,
            String form,
            int level,
            String gender,
            double scale,
            boolean shiny,
            boolean legendary,
            boolean fromSnack,
            String dimension,
            String blockName,
            String biome,
            long ms
    ) {
    }

    private record EncounterResponse(List<EncounterEntry> encounters) {
    }

    public RecentEncountersHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            int limit = Math.min(parseIntParam(params, "limit", DEFAULT_LIMIT), MAX_LIMIT);

            List<EncounterEntry> encounters = query("encounters").timeRange(timeRange)
                    .select("species, form, level, gender, scale_modifier, is_shiny, is_legendary,"
                            + " from_snack, block_name, dimension, biome, encountered_ms")
                    .orderBy("encountered_ms DESC")
                    .limit(limit)
                    .fetchList(rs -> new EncounterEntry(
                            rs.getString("species"),
                            rs.getString("form"),
                            rs.getInt("level"),
                            rs.getString("gender"),
                            rs.getDouble("scale_modifier"),
                            rs.getBoolean("is_shiny"),
                            rs.getBoolean("is_legendary"),
                            rs.getBoolean("from_snack"),
                            rs.getString("dimension"),
                            rs.getString("block_name"),
                            rs.getString("biome"),
                            rs.getLong("encountered_ms")
                    ));

            return new EncounterResponse(encounters);
        });
    }
}