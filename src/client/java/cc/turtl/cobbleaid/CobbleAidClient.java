package cc.turtl.cobbleaid;

import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.config.ConfigManager;
import cc.turtl.cobbleaid.config.CustomLogger;
import cc.turtl.cobbleaid.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;

public class CobbleAidClient implements ClientModInitializer {
    private static CobbleAidClient INSTANCE;

    private final CustomLogger logger;
    private final ConfigManager configManager;
    private final ModConfig config;

    public CobbleAidClient() {
        this.logger = new CustomLogger("cobbleaid");
        this.configManager = new ConfigManager(logger);
        this.config = configManager.getConfig();
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        CobbleAidCommand command = new CobbleAidCommand(configManager, config);
        command.register();
        logger.debug("Commands registered.");

        logger.info("Client initialized.");
    }

    public static CobbleAidClient getInstance() {
        return INSTANCE;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ModConfig getConfig() {
        return config;
    }
}
