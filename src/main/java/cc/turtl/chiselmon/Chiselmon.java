package cc.turtl.chiselmon;

import java.util.List;

import org.apache.logging.log4j.Logger;

import cc.turtl.chiselmon.api.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.command.ChiselmonCommand;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerFeature;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerFeature;
import cc.turtl.chiselmon.service.ConfigService;
import cc.turtl.chiselmon.service.DefaultChiselmonServices;
import cc.turtl.chiselmon.service.IChiselmonServices;
import cc.turtl.chiselmon.service.LoggerService;
import cc.turtl.chiselmon.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Chiselmon implements ClientModInitializer {
    public static final String MODID = ChiselmonConstants.MODID;
    public static final String VERSION = ChiselmonConstants.VERSION;

    private static Chiselmon INSTANCE;
    private volatile IChiselmonServices services;
    private Logger logger;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        initializeServices();
        registerCommands();
        registerFeatures();
        registerListeners();
        logger.info("{} {} initialized.", ChiselmonConstants.MODNAME, ChiselmonConstants.VERSION);
    }

    private void registerCommands() {
        ChiselmonCommand.register();
        logger.debug("Commands registered.");
    }

    private void registerFeatures() {
        final List<AbstractFeature> features = List.of(
                SpawnAlertFeature.getInstance(),
                CheckSpawnTrackerFeature.getInstance(),
                SpawnLoggerFeature.getInstance());
        features.forEach(AbstractFeature::initialize);
    }

    private void initializeServices() {
        LoggerService loggerService = new LoggerService(MODID);
        ConfigService configService = new ConfigService(loggerService.get());
        configService.addListener(cfg -> loggerService.setDebugMode(cfg.debugMode));
        loggerService.setDebugMode(configService.get().debugMode);
        WorldDataService worldDataService = new WorldDataService(configService.get().worldDataMap);
        this.services = new DefaultChiselmonServices(configService, loggerService, worldDataService);
        this.logger = services.logger().get();
        logger.debug("Services initialized.");
    }

    private void registerListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isDisabled())
                return;
            if (client.level != null && !SimpleSpeciesRegistry.isLoaded()) {
                SimpleSpeciesRegistry.loadAsync();
            }
        });
    }

    public void reloadConfig() {
        services.config().reload();
    }

    public void saveConfig() {
        services.config().save();
    }

    public static Chiselmon getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(ChiselmonConstants.MODNAME + " has not been initialized yet!");
        }
        return INSTANCE;
    }

    public static Logger getLogger() {
        return services().logger().get();
    }

    public static IChiselmonServices services() {
        if (getInstance().services == null) {
            throw new IllegalStateException(
                    ChiselmonConstants.MODNAME + " services are not initialized yet; access them after client initialization.");
        }
        return getInstance().services;
    }

    public static boolean isDisabled() {
        return services().config().get().modDisabled;
    }
}
