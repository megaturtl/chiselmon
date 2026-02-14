package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.OLDChiselmonConfig;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {
    public static void initClient() {
        ChiselmonConfig.load();

        ChiselmonRegistries.init();
        AutoConfig.register(OLDChiselmonConfig.class, GsonConfigSerializer::new);
        ChiselmonSystems.init();
    }
}
