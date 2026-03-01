package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class HeatmapHandler extends ApiHandler {

    private record HeatmapPoint(int x, int z) {
    }

    private record HeatmapResponse(int cx, int cz, int radius, String dimension,
                                   List<HeatmapPoint> pokemon, List<HeatmapPoint> player) {
    }

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

            // Spatial bounds
            String pokemonXRange = "pokemon_x BETWEEN " + (cx - radius) + " AND " + (cx + radius);
            String pokemonZRange = "pokemon_z BETWEEN " + (cz - radius) + " AND " + (cz + radius);
            List<HeatmapPoint> pokemonPoints = query("encounters").timeRange(timeRange)
                    .select("pokemon_x, pokemon_z")
                    .where(pokemonXRange)
                    .where(pokemonZRange)
                    .where("dimension = '" + dimension + "'")
                    .fetchList(rs -> new HeatmapPoint(rs.getInt("pokemon_x"), rs.getInt("pokemon_z")));

            List<HeatmapPoint> playerPoints = query("encounters").timeRange(timeRange)
                    .select("player_x, player_z")
                    .where(pokemonXRange)
                    .where(pokemonZRange)
                    .where("dimension = '" + dimension + "'")
                    .fetchList(rs -> new HeatmapPoint(rs.getInt("player_x"), rs.getInt("player_z")));

            return new HeatmapResponse(cx, cz, radius, dimension, pokemonPoints, playerPoints);
        });
    }
}