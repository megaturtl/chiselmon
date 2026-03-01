package cc.turtl.chiselmon.system.dashboard.api.endpoints;

import cc.turtl.chiselmon.system.dashboard.api.ApiHandler;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class PlayerPosHandler extends ApiHandler {

    private record PlayerPositionResponse(int lastX, int lastZ, String lastDimension) {
    }

    private static final PlayerPositionResponse DEFAULT_POSITION =
            new PlayerPositionResponse(0, 0, "minecraft:overworld");

    public PlayerPosHandler(EncounterDatabase db) {
        super(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (timeRange, params) -> {
            PlayerPositionResponse position = query("encounters").timeRange(timeRange)
                    .select("player_x, player_z, dimension")
                    .orderBy("encountered_ms DESC")
                    .limit(1)
                    .fetchOne(rs -> new PlayerPositionResponse(
                            rs.getInt("player_x"),
                            rs.getInt("player_z"),
                            rs.getString("dimension")
                    ));

            return position != null ? position : DEFAULT_POSITION;
        });
    }
}