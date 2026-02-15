package cc.turtl.chiselmon.worlddata;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages per-world data storage.
 * Loads on world join, saves on changes, clears on world leave.
 */
public class WorldDataManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .serializeNulls()
            .create();

    private static Path dataDirectory;
    private static WorldData currentWorldData;
    private static String currentWorldId;

    /**
     * Initializes the data directory and subscribes to events.
     */
    public static void init() {
        dataDirectory = ChiselmonConstants.CONFIG_PATH.resolve("worlddata");
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to create world data directory", e);
        }

        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(e -> {
            WorldDataManager.saveAndClear();
        });

        ChiselmonEvents.GAME_STOPPING.subscribe(e -> {
            WorldDataManager.saveAndClear();
        });
    }

    /**
     * Gets world data for the current world.
     * Loads from disk if not already loaded.
     */
    @NotNull
    public static WorldData get() {
        String worldId = getWorldId();

        // Load if not cached or world changed
        if (currentWorldData == null || !worldId.equals(currentWorldId)) {
            currentWorldId = worldId;
            currentWorldData = load(worldId);
        }

        return currentWorldData;
    }

    /**
     * Saves current world data to disk.
     */
    public static void save() {
        if (currentWorldData == null || currentWorldId == null) {
            ChiselmonConstants.LOGGER.warn("No world data to save");
            return;
        }

        Path file = getDataFile(currentWorldId);

        try {
            String json = GSON.toJson(currentWorldData);
            Files.writeString(file, json);
            ChiselmonConstants.LOGGER.debug("Saved world data for {}", currentWorldId);
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to save world data for {}", currentWorldId, e);
        }
    }

    /**
     * Saves and then clears cached world data.
     */
    public static void saveAndClear() {
        save();
        currentWorldData = null;
        currentWorldId = null;
    }

    @NotNull
    private static String getWorldId() {
        String worldName = getWorldName();
        if (worldName != null && !worldName.isEmpty()) {
            return sanitizeFileName(worldName);
        }
        return "fallback";
    }

    @Nullable
    private static String getWorldName() {
        Minecraft minecraft = Minecraft.getInstance();

        // Singleplayer - use world name
        if (minecraft.getSingleplayerServer() != null) {
            return minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }

        // Multiplayer - use server IP
        if (minecraft.getCurrentServer() != null) {
            return minecraft.getCurrentServer().ip;
        }

        return null;
    }

    @NotNull
    private static String sanitizeFileName(@NotNull String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @NotNull
    private static Path getDataFile(@NotNull String worldId) {
        return dataDirectory.resolve(worldId + ".json");
    }

    @NotNull
    private static WorldData load(@NotNull String worldId) {
        Path file = getDataFile(worldId);

        if (!Files.exists(file)) {
            ChiselmonConstants.LOGGER.info("Creating new world data for {}", worldId);
            return new WorldData();
        }

        try {
            String json = Files.readString(file);
            WorldData data = GSON.fromJson(json, WorldData.class);

            if (data == null) {
                ChiselmonConstants.LOGGER.warn("World data file was empty for {}, creating new", worldId);
                return new WorldData();
            }

            ChiselmonConstants.LOGGER.info("Loaded world data for {}", worldId);
            return data;

        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to read world data for {}", worldId, e);
            return new WorldData();
        } catch (JsonSyntaxException e) {
            ChiselmonConstants.LOGGER.error("Failed to parse world data for {}, creating backup", worldId, e);
            backupCorruptedFile(file);
            return new WorldData();
        }
    }

    private static void backupCorruptedFile(@NotNull Path file) {
        try {
            Path backup = file.getParent().resolve(file.getFileName() + ".corrupted." + System.currentTimeMillis());
            Files.move(file, backup);
            ChiselmonConstants.LOGGER.info("Backed up corrupted file to {}", backup);
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to backup corrupted file", e);
        }
    }
}