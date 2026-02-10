package cc.turtl.chiselmon;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.ChiselmonConfigNew;
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

        // Initialize the new custom config system
        ChiselmonConfigNew.init();
        LOGGER.info("[Chiselmon] New config system initialized");

        // Keep old AutoConfig for now during transition
        AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
        LOGGER.info("[Chiselmon] AutoConfig registered (legacy)");

        ChiselmonSystems.init();
        LOGGER.info("[Chiselmon] Systems initialized");

        // Sync dynamic groups to new config
        ChiselmonConfigNew.get().alert().syncGroupsFromRegistry();
        ChiselmonConfigNew.get().save();
        LOGGER.info("[Chiselmon] Groups synced to new config");
    }
}
