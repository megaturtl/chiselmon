package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.category.AlertCategory;
import cc.turtl.chiselmon.config.option.BooleanOption;
import cc.turtl.chiselmon.config.option.DoubleOption;
import cc.turtl.chiselmon.config.option.IntegerOption;
import cc.turtl.chiselmon.config.serialization.ConfigSerializer;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * New custom config system for Chiselmon.
 * This replaces the AutoConfig-based system to support dynamic groups and advanced widgets.
 */
public class ChiselmonConfigNew {

    private static final Logger LOGGER = ChiselmonConstants.LOGGER;
    private static ChiselmonConfigNew INSTANCE;
    private final ConfigSerializer serializer;

    // Categories
    private final GeneralCategory general = new GeneralCategory();
    private final ThresholdCategory threshold = new ThresholdCategory();
    private final AlertCategory alert = new AlertCategory();

    private ChiselmonConfigNew(Path configPath) {
        this.serializer = new ConfigSerializer(configPath, this);
    }

    /**
     * Initializes the config system. Must be called during mod initialization.
     */
    public static void init() {
        Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("chiselmon");
        Path configPath = configDir.resolve("config.json");

        INSTANCE = new ChiselmonConfigNew(configPath);
        INSTANCE.load();

        LOGGER.info("[ChiselmonConfigNew] Config system initialized");
    }

    /**
     * Initializes the config system with a custom path.
     */
    public static void init(Path configPath) {
        INSTANCE = new ChiselmonConfigNew(configPath);
        INSTANCE.load();

        LOGGER.info("[ChiselmonConfigNew] Config system initialized at: {}", configPath);
    }

    /**
     * Gets the singleton instance. Must call init() first.
     */
    public static ChiselmonConfigNew get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ChiselmonConfigNew not initialized! Call init() first.");
        }
        return INSTANCE;
    }

    /**
     * Checks if the config has been initialized
     */
    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    public void load() {
        serializer.load();
    }

    public void save() {
        serializer.save();
    }

    /**
     * Syncs dynamic content (like groups) and saves.
     */
    public void syncAndSave() {
        alert.syncGroupsFromRegistry();
        save();
    }

    // Category accessors
    public GeneralCategory general() {
        return general;
    }

    public ThresholdCategory threshold() {
        return threshold;
    }

    public AlertCategory alert() {
        return alert;
    }

    /**
     * General settings category
     */
    public static class GeneralCategory {
        public final BooleanOption modDisabled = BooleanOption.builder()
                .name("modDisabled")
                .displayName("Disable Mod")
                .comment("Completely disable all mod functionality")
                .defaultValue(false)
                .build();
    }

    /**
     * Threshold settings category
     */
    public static class ThresholdCategory {
        public final DoubleOption extremeSmall = DoubleOption.builder()
                .name("extremeSmall")
                .displayName("Extreme Small Threshold")
                .comment("Size multiplier below which a Pokemon is considered 'extremely small'")
                .defaultValue(0.4)
                .range(0.0, 1.0)
                .build();

        public final DoubleOption extremeLarge = DoubleOption.builder()
                .name("extremeLarge")
                .displayName("Extreme Large Threshold")
                .comment("Size multiplier above which a Pokemon is considered 'extremely large'")
                .defaultValue(1.6)
                .range(1.0, 3.0)
                .build();

        public final IntegerOption maxIvs = IntegerOption.builder()
                .name("maxIvs")
                .displayName("Max IVs Threshold")
                .comment("Number of perfect IVs required to be considered 'high IV'")
                .defaultValue(5)
                .range(1, 6)
                .build();
    }
}
