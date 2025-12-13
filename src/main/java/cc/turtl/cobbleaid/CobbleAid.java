package cc.turtl.cobbleaid;

import org.apache.logging.log4j.Logger;

import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.service.ICobbleAidServices;
import cc.turtl.cobbleaid.service.ConfigService;
import cc.turtl.cobbleaid.service.DefaultCobbleAidServices;
import cc.turtl.cobbleaid.service.LoggerService;
import cc.turtl.cobbleaid.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;

public class CobbleAid implements ClientModInitializer {
    public static final String MODID = "cobbleaid";
    public static final String VERSION = "1.0.1";

    private static CobbleAid INSTANCE;
    private volatile ICobbleAidServices services;
    private Logger logger;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        initializeServices();
        registerCommands();
        registerListeners();
        logger.info("Cobble Aid {} initialized.", VERSION);
    }

    private void registerCommands() {
        CobbleAidCommand.register();
        logger.debug("Commands registered.");
    }

    private void registerListeners() {
    }

    private void initializeServices() {
        LoggerService loggerService = new LoggerService(MODID);
        ConfigService configService = new ConfigService(loggerService.get());
        configService.addListener(cfg -> loggerService.setDebugMode(cfg.debugMode));
        loggerService.setDebugMode(configService.get().debugMode);
        WorldDataService worldDataService = new WorldDataService(configService.get().worldDataMap);
        this.services = new DefaultCobbleAidServices(configService, loggerService, worldDataService);
        this.logger = services.logger().get();
        this.logger.debug("Services initialized.");
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
            throw new IllegalStateException("Cobble Aid services are not initialized yet; access them after client initialization.");
        }
        return getInstance().services;
    }

    /**
     * @deprecated Use {@link #services()} and {@link cc.turtl.cobbleaid.service.ConfigService} for configuration access.
     */
    @Deprecated
    public ModConfig getConfig() {
        return services().config().get();
    }

    /**
     * @deprecated Use {@link #services()} and {@link cc.turtl.cobbleaid.service.WorldDataService} to access per-world data.
     */
    @Deprecated
    public WorldDataStore getWorldData() {
        return services().worldData().current();
    }
}
