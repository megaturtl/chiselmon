package cc.turtl.chiselmon;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Chiselmon {
    private static final Logger LOGGER = LogManager.getLogger(ChiselmonConstants.MOD_ID);

    public static void initClient() {
        LOGGER.info("[Chiselmon] initClient() starting");
        ChiselmonRegistries.init();
        LOGGER.info("[Chiselmon] Registries initialized");
        AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
        LOGGER.info("[Chiselmon] AutoConfig registered");
        ChiselmonSystems.init();
        LOGGER.info("[Chiselmon] Systems initialized");
    }
}
