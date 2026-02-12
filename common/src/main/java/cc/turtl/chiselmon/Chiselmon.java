package cc.turtl.chiselmon;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.ChiselmonConfigHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfigHandler.load();

        ChiselmonRegistries.init();
        AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
        ChiselmonSystems.init();
    }
}
