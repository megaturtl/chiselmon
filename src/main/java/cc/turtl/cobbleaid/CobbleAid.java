package cc.turtl.cobbleaid;

import java.util.List;

import org.apache.logging.log4j.Logger;

import cc.turtl.cobbleaid.api.SimpleSpeciesRegistry;
import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.feature.AbstractFeature;
import cc.turtl.cobbleaid.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.cobbleaid.feature.spawnlogger.SpawnLoggerFeature;
import cc.turtl.cobbleaid.feature.checkspawntracker.CheckSpawnTrackerFeature;
import cc.turtl.cobbleaid.service.ICobbleAidServices;
import cc.turtl.cobbleaid.service.ConfigService;
import cc.turtl.cobbleaid.service.DefaultCobbleAidServices;
import cc.turtl.cobbleaid.service.LoggerService;
import cc.turtl.cobbleaid.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class CobbleAid implements ClientModInitializer {
    public static final String MODID = "cobbleaid";
    public static final String VERSION = "1.0.3-alpha";

    private static CobbleAid INSTANCE;
    private volatile ICobbleAidServices services;
    private Logger logger;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        initializeServices();
        registerCommands();
        registerFeatures();
        registerListeners();
        logger.info("Cobble Aid {} initialized.", VERSION);
    }

    private void registerCommands() {
        CobbleAidCommand.register();
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
        this.services = new DefaultCobbleAidServices(configService, loggerService, worldDataService);
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

    public static CobbleAid getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Cobble Aid has not been initialized yet!");
        }
        return INSTANCE;
    }

    public static Logger getLogger() {
        return services().logger().get();
    }

    public static ICobbleAidServices services() {
        if (getInstance().services == null) {
            throw new IllegalStateException(
                    "Cobble Aid services are not initialized yet; access them after client initialization.");
        }
        return getInstance().services;
    }

    public static boolean isDisabled() {
        return services().config().get().modDisabled;
    }
}
