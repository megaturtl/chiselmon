package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HeatmapHandler extends ApiHandler {

    /**
     * Points are encoded as a flat interleaved array: [x1, z1, x2, z2, ...].
     * This halves the JSON payload vs [{x,z}, ...] by eliminating repeated key
     * names — significant at scale where heatmaps can return thousands of points.
     * <p>
     * Client decodes with: for (let i = 0; i < arr.length; i += 2) use(arr[i], arr[i+1])
     */
    private record FetchBounds(int minX, int maxX, int minZ, int maxZ) {
    }

    private record HeatmapResponse(
            int cx, int cz, int radius, String dimension,
            FetchBounds fetchBounds,
            int[] pokemon,
            int[] player
    ) {}

    public HeatmapHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            int cx = requireIntParam(params, "cx", 0);
            int cz = requireIntParam(params, "cz", 0);
            int radius = requireIntParam(params, "radius", 256);
            radius = Math.max(16, Math.min(radius, 4096));

            String dimension = params.getOrDefault("dimension", "minecraft:overworld");

            int minX = cx - radius;
            int maxX = cx + radius;
            int minZ = cz - radius;
            int maxZ = cz + radius;

            String xRange = "pokemon_x BETWEEN " + minX + " AND " + maxX;
            String zRange = "pokemon_z BETWEEN " + minZ + " AND " + maxZ;
            String dim = "dimension = '" + dimension + "'";

            int[] pokemon = query("encounters").timeRange(timeRange)
                    .select("pokemon_x, pokemon_z")
                    .where(xRange).where(zRange).where(dim)
                    .fetchInterleavedPairs("pokemon_x", "pokemon_z");

            int[] player = query("encounters").timeRange(timeRange)
                    .select("player_x, player_z")
                    .where(xRange).where(zRange).where(dim)
                    .fetchInterleavedPairs("player_x", "player_z");

            return new HeatmapResponse(cx, cz, radius, dimension,
                    new FetchBounds(minX, maxX, minZ, maxZ),
                    pokemon, player);
        });
    }
}