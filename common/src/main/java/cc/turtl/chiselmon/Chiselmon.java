package cc.turtl.chiselmon;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonRegistries.init();
        AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
        ChiselmonSystems.init();
        // Sync groups now that systems are initialized
        ChiselmonConstants.CONFIG.alert.syncGroupsFromRegistry();
    }
}
