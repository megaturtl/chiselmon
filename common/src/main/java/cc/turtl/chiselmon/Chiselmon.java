package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.filter.FiltersUserData;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.PCUserData;
import cc.turtl.chiselmon.userdata.DataScope;
import cc.turtl.chiselmon.userdata.UserDataRegistry;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.load();
        ChiselmonSystems.init();

        initRegistries();
    }

    private static void initRegistries() {
        ClientSpeciesRegistry.init();

        // Initialize user data registry with our user data classes
        UserDataRegistry.init(ChiselmonConstants.CONFIG_PATH);
        UserDataRegistry.register(PCUserData.class, "pcdata", DataScope.WORLD, PCUserData::new);
        UserDataRegistry.register(FiltersUserData.class, "filters", DataScope.GLOBAL, FiltersUserData::withDefaults);
    }
}
