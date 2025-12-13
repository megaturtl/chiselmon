package cc.turtl.cobbleaid;

import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.service.CobbleAidServices;
import cc.turtl.cobbleaid.service.ConfigService;
import cc.turtl.cobbleaid.service.DefaultCobbleAidServices;
import cc.turtl.cobbleaid.service.LoggingService;
import cc.turtl.cobbleaid.service.WorldDataService;
import net.fabricmc.api.ClientModInitializer;

public class CobbleAid implements ClientModInitializer {
    public static final String MODID = "cobbleaid";
    public static final String VERSION = "1.0.1";

    private static CobbleAid INSTANCE;
    private CobbleAidServices services;
    private CobbleAidLogger logger;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        initializeServices();
        registerCommands();
        registerListeners();
        logger.info("Cobble Aid " + VERSION + " initialized.");
    }

    private void registerCommands() {
        CobbleAidCommand.register();
        logger.debug("Commands registered.");
    }

    private void registerListeners() {
    }

    private void initializeServices() {
        LoggingService loggingService = new LoggingService(MODID);
        ConfigService configService = new ConfigService(loggingService.getLogger(CobbleAid.class));
        configService.addListener(cfg -> loggingService.setDebugMode(cfg.debugMode));
        loggingService.setDebugMode(configService.get().debugMode);
        WorldDataService worldDataService = new WorldDataService(configService.get().worldDataMap);
        this.services = new DefaultCobbleAidServices(configService, loggingService, worldDataService);
        this.logger = this.services.logger(CobbleAid.class);
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

    public static CobbleAidLogger getLogger() {
        return services().logger(CobbleAid.class);
    }

    public static CobbleAidServices services() {
        if (getInstance().services == null) {
            throw new IllegalStateException("Cobble Aid services have not been initialized yet!");
        }
        return getInstance().services;
    }

    public ModConfig getConfig() {
        return services().config().get();
    }

    public WorldDataStore getWorldData() {
        return services().worldData().current();
    }
}
