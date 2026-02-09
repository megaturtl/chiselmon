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
        LOGGER.info("[Chiselmon] Systems initialized, pokemonGroups null? {}", ChiselmonSystems.pokemonGroups() == null);
        
        // Sync groups now that systems are initialized
        LOGGER.info("[Chiselmon] About to sync groups. Current groups count: {}", ChiselmonConstants.CONFIG.alert.groups.size());
        ChiselmonConstants.CONFIG.alert.syncGroupsFromRegistry();
        LOGGER.info("[Chiselmon] After sync, groups count: {}", ChiselmonConstants.CONFIG.alert.groups.size());
        
        // Save config to persist the synced groups
        ChiselmonConstants.CONFIG_HOLDER.save();
        LOGGER.info("[Chiselmon] Config saved");
    }
}
