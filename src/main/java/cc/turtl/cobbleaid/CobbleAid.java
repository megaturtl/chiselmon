package cc.turtl.cobbleaid;

import cc.turtl.cobbleaid.command.CobbleAidCommand;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.core.lifecycle.FeatureManager;
import cc.turtl.cobbleaid.core.registry.RegistryHelper;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.InteractionResult;

/**
 * Main mod initializer for CobbleAid.
 * <p>
 * This class is the entry point for the mod and manages the lifecycle
 * of all features, configuration, and world data.
 * </p>
 */
public class CobbleAid implements ClientModInitializer {
    public static final String MODID = "cobbleaid";
    public static final String VERSION = "1.0.1";

    private static final CobbleAidLogger LOGGER = new CobbleAidLogger(MODID);

    private static CobbleAid INSTANCE;

    private ConfigHolder<ModConfig> configHolder;
    private ModConfig config;
    private WorldDataManager worldManager;
    private FeatureManager featureManager;
    private RegistryHelper registryHelper;
    
    // Feature instances for easy access
    private cc.turtl.cobbleaid.feature.hud.HudFeature hudFeature;
    private cc.turtl.cobbleaid.feature.pc.icons.PcIconsFeature pcIconsFeature;
    private cc.turtl.cobbleaid.feature.pc.sorting.PcSortingFeature pcSortingFeature;
    private cc.turtl.cobbleaid.feature.pc.tabs.PcTabsFeature pcTabsFeature;
    private cc.turtl.cobbleaid.feature.pc.tooltips.PcTooltipsFeature pcTooltipsFeature;
    private cc.turtl.cobbleaid.feature.pc.eggs.PcEggsFeature pcEggsFeature;

    public CobbleAid() {
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        preInitialize();
        initialize();

        LOGGER.info("Cobble Aid " + VERSION + " initialized.");
    }

    private void preInitialize() {
        loadConfig();
        initializeWorldManager();
        initializeInfrastructure();

        LOGGER.debug("Pre-initialization complete.");
    }

    private void initialize() {
        registerFeatures();
        registerCommands();
        featureManager.initializeAll();
        LOGGER.debug("Initialization complete.");
    }
    
    private void initializeInfrastructure() {
        this.featureManager = new FeatureManager(LOGGER);
        this.registryHelper = new RegistryHelper(LOGGER);
        LOGGER.debug("Core infrastructure initialized.");
    }
    
    private void registerFeatures() {
        // Register all features here
        
        // Demo feature (for developer reference)
        featureManager.register(new cc.turtl.cobbleaid.feature.demo.DemoFeature());
        
        // HUD features
        this.hudFeature = new cc.turtl.cobbleaid.feature.hud.HudFeature();
        featureManager.register(hudFeature);
        
        // PC features
        this.pcIconsFeature = new cc.turtl.cobbleaid.feature.pc.icons.PcIconsFeature();
        featureManager.register(pcIconsFeature);
        
        this.pcSortingFeature = new cc.turtl.cobbleaid.feature.pc.sorting.PcSortingFeature();
        featureManager.register(pcSortingFeature);
        
        this.pcTabsFeature = new cc.turtl.cobbleaid.feature.pc.tabs.PcTabsFeature();
        featureManager.register(pcTabsFeature);
        
        this.pcTooltipsFeature = new cc.turtl.cobbleaid.feature.pc.tooltips.PcTooltipsFeature();
        featureManager.register(pcTooltipsFeature);
        
        this.pcEggsFeature = new cc.turtl.cobbleaid.feature.pc.eggs.PcEggsFeature();
        featureManager.register(pcEggsFeature);
        
        LOGGER.debug("Features registered.");
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

    private void initializeWorldManager() {
        // Pass the persistent map from the loaded config to the WorldManager
        // constructor.
        this.worldManager = new WorldDataManager(this.config.worldDataMap);
        LOGGER.debug("WorldManager initialized with persistent data.");
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
        if (INSTANCE == null) {
            throw new IllegalStateException("Cobble Aid has not been initialized yet!");
        }
        return INSTANCE;
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

    public WorldDataStore getWorldData() {
        return worldManager.getOrCreateStore();
    }
    
    public FeatureManager getFeatureManager() {
        return featureManager;
    }
    
    public RegistryHelper getRegistryHelper() {
        return registryHelper;
    }
    
    // Feature accessors - provides single source of truth for feature state
    
    public cc.turtl.cobbleaid.feature.hud.HudFeature getHudFeature() {
        return hudFeature;
    }
    
    public cc.turtl.cobbleaid.feature.pc.icons.PcIconsFeature getPcIconsFeature() {
        return pcIconsFeature;
    }
    
    public cc.turtl.cobbleaid.feature.pc.sorting.PcSortingFeature getPcSortingFeature() {
        return pcSortingFeature;
    }
    
    public cc.turtl.cobbleaid.feature.pc.tabs.PcTabsFeature getPcTabsFeature() {
        return pcTabsFeature;
    }
    
    public cc.turtl.cobbleaid.feature.pc.tooltips.PcTooltipsFeature getPcTooltipsFeature() {
        return pcTooltipsFeature;
    }
    
    public cc.turtl.cobbleaid.feature.pc.eggs.PcEggsFeature getPcEggsFeature() {
        return pcEggsFeature;
    }
}