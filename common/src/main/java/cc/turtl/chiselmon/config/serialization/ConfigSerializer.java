package cc.turtl.chiselmon.config.serialization;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.ChiselmonConfigNew;
import cc.turtl.chiselmon.config.option.ConfigOption;
import cc.turtl.chiselmon.config.option.GroupAlertOption;
import com.google.gson.*;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles loading and saving config to JSON files.
 */
public class ConfigSerializer {

    private static final Logger LOGGER = ChiselmonConstants.LOGGER;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path configPath;
    private final ChiselmonConfigNew config;

    public ConfigSerializer(Path configPath, ChiselmonConfigNew config) {
        this.configPath = configPath;
        this.config = config;
    }

    /**
     * Loads the config from the JSON file.
     * Creates a default config file if it doesn't exist.
     */
    public void load() {
        if (!Files.exists(configPath)) {
            LOGGER.info("[ConfigSerializer] Config file not found, creating default at: {}", configPath);
            save(); // Create default config
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            loadFromJson(root);
            LOGGER.info("[ConfigSerializer] Config loaded from: {}", configPath);
        } catch (Exception e) {
            LOGGER.error("[ConfigSerializer] Failed to load config from: {}", configPath, e);
        }
    }

    /**
     * Saves the config to the JSON file.
     */
    public void save() {
        try {
            // Ensure parent directories exist
            Files.createDirectories(configPath.getParent());

            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                JsonObject root = toJson();
                GSON.toJson(root, writer);
            }
            LOGGER.info("[ConfigSerializer] Config saved to: {}", configPath);
        } catch (Exception e) {
            LOGGER.error("[ConfigSerializer] Failed to save config to: {}", configPath, e);
        }
    }

    private void loadFromJson(JsonObject root) {
        // Load general settings
        if (root.has("general")) {
            JsonObject general = root.getAsJsonObject("general");
            loadOption(config.general().modDisabled, general, "modDisabled");
        }

        // Load threshold settings
        if (root.has("threshold")) {
            JsonObject threshold = root.getAsJsonObject("threshold");
            loadOption(config.threshold().extremeSmall, threshold, "extremeSmall");
            loadOption(config.threshold().extremeLarge, threshold, "extremeLarge");
            loadOption(config.threshold().maxIvs, threshold, "maxIvs");
        }

        // Load alert settings
        if (root.has("alert")) {
            JsonObject alert = root.getAsJsonObject("alert");
            loadOption(config.alert().masterEnabled, alert, "masterEnabled");
            loadOption(config.alert().masterVolume, alert, "masterVolume");
            loadOption(config.alert().showFormInMessage, alert, "showFormInMessage");

            // Load dynamic group alerts
            if (alert.has("groups")) {
                JsonObject groups = alert.getAsJsonObject("groups");
                for (String groupId : groups.keySet()) {
                    GroupAlertOption groupOption = config.alert().getGroupAlert(groupId);
                    groupOption.fromJson(groups.get(groupId));
                }
            }
        }
    }

    private JsonObject toJson() {
        JsonObject root = new JsonObject();

        // General settings
        JsonObject general = new JsonObject();
        general.add("modDisabled", config.general().modDisabled.toJson());
        root.add("general", general);

        // Threshold settings
        JsonObject threshold = new JsonObject();
        threshold.add("extremeSmall", config.threshold().extremeSmall.toJson());
        threshold.add("extremeLarge", config.threshold().extremeLarge.toJson());
        threshold.add("maxIvs", config.threshold().maxIvs.toJson());
        root.add("threshold", threshold);

        // Alert settings
        JsonObject alert = new JsonObject();
        alert.add("masterEnabled", config.alert().masterEnabled.toJson());
        alert.add("masterVolume", config.alert().masterVolume.toJson());
        alert.add("showFormInMessage", config.alert().showFormInMessage.toJson());

        // Dynamic group alerts
        JsonObject groups = new JsonObject();
        for (GroupAlertOption groupOption : config.alert().getGroupAlerts()) {
            groups.add(groupOption.getGroupId(), groupOption.toJson());
        }
        alert.add("groups", groups);

        root.add("alert", alert);

        return root;
    }

    private void loadOption(ConfigOption<?> option, JsonObject parent, String key) {
        if (parent.has(key)) {
            option.fromJson(parent.get(key));
        }
    }
}
