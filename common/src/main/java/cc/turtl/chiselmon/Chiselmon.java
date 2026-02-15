package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.worlddata.WorldDataManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.load();
        ChiselmonConfig.get().filter.ensureDefaults();
        FilterRegistry.loadFromConfig();
        WorldDataManager.init();
        ChiselmonRegistries.init();
        ChiselmonSystems.init();
    }
}
