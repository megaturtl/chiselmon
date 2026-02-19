package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.filter.FiltersUserData;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.PCUserData;
import cc.turtl.chiselmon.api.DataScope;
import cc.turtl.chiselmon.data.UserDataRegistry;
import cc.turtl.chiselmon.system.alert.AlertManager;
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager;
import cc.turtl.chiselmon.system.tracker.TrackerManager;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.init();
        initRegistries();

        TrackerManager.getInstance().init();
        AlertManager.getInstance().init();
        SpawnRecorderManager.getInstance().init();
    }

    private static void initRegistries() {
        ClientSpeciesRegistry.init();

        // Initialize user data registry with our user data classes
        UserDataRegistry.init(ChiselmonConstants.CONFIG_PATH);
        UserDataRegistry.register(PCUserData.class, "pcdata", DataScope.WORLD, PCUserData::new);
        UserDataRegistry.register(FiltersUserData.class, "filters", DataScope.GLOBAL, FiltersUserData::withDefaults);

    }
}
