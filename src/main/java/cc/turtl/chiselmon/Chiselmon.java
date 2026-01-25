package cc.turtl.chiselmon;

import java.util.List;

import org.apache.logging.log4j.Logger;

import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.module.ModuleRegistry;
import cc.turtl.chiselmon.module.feature.CheckSpawnTrackerModule;
import cc.turtl.chiselmon.module.feature.EggPreviewModule;
import cc.turtl.chiselmon.module.feature.SpawnAlertModule;
import cc.turtl.chiselmon.module.feature.SpawnLoggerModule;
import cc.turtl.chiselmon.service.ConfigService;
import cc.turtl.chiselmon.service.DefaultChiselmonServices;
import cc.turtl.chiselmon.service.IChiselmonServices;
import cc.turtl.chiselmon.service.LoggerService;
import cc.turtl.chiselmon.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Chiselmon implements ClientModInitializer {
    private static Chiselmon INSTANCE;
    private volatile IChiselmonServices services;
    private Logger logger;
    private ModuleRegistry moduleRegistry;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        initializeServices();
        registerModules();
        registerCommands();
        registerListeners();
        logger.info("{} {} initialized.", ChiselmonConstants.MODNAME, ChiselmonConstants.VERSION);
    }

    private void registerCommands() {
        ChiselmonCommand.register();
        logger.debug("Commands registered.");
    }

    private void registerModules() {
        moduleRegistry = new ModuleRegistry(logger);
        moduleRegistry.register(new SpawnAlertModule());
        moduleRegistry.register(new CheckSpawnTrackerModule());
        moduleRegistry.register(new SpawnLoggerModule());
        moduleRegistry.register(new EggPreviewModule());
        moduleRegistry.initializeModules();
        logger.debug("Modules registered.");
    }

    private void initializeServices() {
        LoggerService loggerService = new LoggerService(ChiselmonConstants.MODID);
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

    public static ModuleRegistry modules() {
        if (getInstance().moduleRegistry == null) {
            throw new IllegalStateException(
                    ChiselmonConstants.MODNAME + " modules are not initialized yet; access them after client initialization.");
        }
        return getInstance().moduleRegistry;
    }
}
