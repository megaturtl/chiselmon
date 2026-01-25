package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerFeature;
import cc.turtl.chiselmon.feature.eggpreview.EggPreviewFeature;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerFeature;
import cc.turtl.chiselmon.service.ConfigService;
import cc.turtl.chiselmon.service.DefaultChiselmonServices;
import cc.turtl.chiselmon.service.IChiselmonServices;
import cc.turtl.chiselmon.service.LoggerService;
import cc.turtl.chiselmon.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ChiselmonFabric implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        Chiselmon.init(this::initializeServices, this::registerCommands, this::registerFeatures, this::registerListeners);
    }

    private void initializeServices() {
        LoggerService loggerService = new LoggerService(ChiselmonConstants.MODID);
        ConfigService configService = new ConfigService(loggerService.get());
        configService.addListener(cfg -> loggerService.setDebugMode(cfg.debugMode));
        loggerService.setDebugMode(configService.get().debugMode);
        WorldDataService worldDataService = new WorldDataService(configService.get().worldDataMap);
        IChiselmonServices services = new DefaultChiselmonServices(configService, loggerService, worldDataService);
        Chiselmon.setServices(services);
        Logger logger = services.logger().get();
        logger.debug("Services initialized.");
    }

    private void registerCommands() {
        ChiselmonCommand.register();
        Chiselmon.getLogger().debug("Commands registered.");
    }

    private void registerFeatures() {
        final List<AbstractFeature> features = List.of(
                SpawnAlertFeature.getInstance(),
                CheckSpawnTrackerFeature.getInstance(),
                SpawnLoggerFeature.getInstance(),
                EggPreviewFeature.getInstance());
        features.forEach(AbstractFeature::initialize);
    }

    private void registerListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Chiselmon.isDisabled())
                return;
            if (client.level != null && !SimpleSpeciesRegistry.isLoaded()) {
                SimpleSpeciesRegistry.loadAsync();
            }
        });
    }
}
