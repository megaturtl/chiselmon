package cc.turtl.chiselmon.userdata;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles JSON serialization and deserialization for persistent data.
 */
public class JsonSerializer {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .serializeNulls()
            .create();

    /**
     * Saves data to a JSON file.
     */
    public static <T> boolean save(@NotNull Path file, @NotNull T data, @NotNull Logger logger) {
        try {
            // Ensure parent directory exists
            Files.createDirectories(file.getParent());

            String json = GSON.toJson(data);
            Files.writeString(file, json);

            logger.debug("Saved data to {}", file);
            return true;

        } catch (IOException e) {
            logger.error("Failed to save data to {}", file, e);
            return false;
        }
    }

    /**
     * Loads data from a JSON file.
     * Returns null if the file doesn't exist or cannot be parsed.
     */
    @Nullable
    public static <T> T load(@NotNull Path file, @NotNull Class<T> clazz, @NotNull Logger logger) {
        if (!Files.exists(file)) {
            logger.debug("Data file does not exist: {}", file);
            return null;
        }

        try {
            String json = Files.readString(file);
            T data = GSON.fromJson(json, clazz);

            if (data == null) {
                logger.warn("Data file was empty: {}", file);
                return null;
            }

            logger.debug("Loaded data from {}", file);
            return data;

        } catch (IOException e) {
            logger.error("Failed to read data from {}", file, e);
            return null;

        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse data from {}, creating backup", file, e);
            backupCorruptedFile(file, logger);
            return null;
        }
    }

    private static void backupCorruptedFile(@NotNull Path file, @NotNull Logger logger) {
        try {
            Path backup = file.getParent().resolve(
                    file.getFileName() + ".corrupted." + System.currentTimeMillis()
            );
            Files.move(file, backup);
            logger.info("Backed up corrupted file to {}", backup);
        } catch (IOException e) {
            logger.error("Failed to backup corrupted file", e);
        }
    }
}