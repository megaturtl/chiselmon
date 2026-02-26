package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.chat.CheckSpawnInterceptor;
import cc.turtl.chiselmon.system.alert.AlertManager;
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager;
import cc.turtl.chiselmon.system.tracker.TrackerManager;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.init();
        ClientSpeciesRegistry.init();
        ChiselmonStorage.init();

        TrackerManager.getInstance().init();
        AlertManager.getInstance().init();
        SpawnRecorderManager.getInstance().init();

        CheckSpawnInterceptor.init();
    }
}
