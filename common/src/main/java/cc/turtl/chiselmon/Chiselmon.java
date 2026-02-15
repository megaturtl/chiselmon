package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.OLDChiselmonConfig;
import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.worlddata.WorldDataManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.load();
        FilterRegistry.loadFromConfig();
        WorldDataManager.init();
        AutoConfig.register(OLDChiselmonConfig.class, GsonConfigSerializer::new);
        ChiselmonRegistries.init();
        ChiselmonSystems.init();
    }
}
