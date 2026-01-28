package cc.turtl.chiselmon;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class Chiselmon {

    public static void initClient() {
        AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
    }
}
