package cc.turtl.cobbleaid;

import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.InteractionResult;

public class CobbleAid implements ClientModInitializer {
    public static final String MODID = "cobbleaid";
    public static final String VERSION = "1.0.0";
    
    private static final CobbleAidLogger LOGGER = new CobbleAidLogger(MODID);

    private static CobbleAid instance;
    
    private ConfigHolder<ModConfig> configHolder;
    private ModConfig config;

    public CobbleAid() {
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        
        preInitialize();
        initialize();
        
        LOGGER.info("Cobble Aid " + VERSION + " initialized.");
    }

    private void preInitialize() {
        loadConfig();
        
        LOGGER.debug("Pre-initialization complete.");
    }

    private void initialize() {
        registerCommands();
        LOGGER.debug("Initialization complete.");
    }

    private void registerCommands() {
        CobbleAidCommand.register();
        LOGGER.debug("Commands registered.");
    }

    private void loadConfig() {
        this.configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        configHolder.registerSaveListener(this::onConfigSave);
        
        this.config = configHolder.getConfig();
        LOGGER.setDebugMode(config.debugMode);
    }

    private InteractionResult onConfigSave(ConfigHolder<ModConfig> manager, ModConfig data) {
        data.validate_fields();
        LOGGER.setDebugMode(data.debugMode);
        LOGGER.debug("Configuration saved successfully.");
        return InteractionResult.SUCCESS;
    }

    public void reloadConfig() {
        this.config = configHolder.getConfig();
        LOGGER.setDebugMode(config.debugMode);
        LOGGER.info("Configuration reloaded.");
    }

    public void saveConfig() {
        configHolder.save();
    }

    public static CobbleAid getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cobble Aid has not been initialized yet!");
        }
        return instance;
    }

    public static CobbleAidLogger getLogger() {
        return LOGGER;
    }

    public ConfigHolder<ModConfig> getConfigHolder() {
        return configHolder;
    }

    public ModConfig getConfig() {
        return config;
    }
}