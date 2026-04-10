package cc.turtl.chiselmon.api.storage.adapter;

import cc.turtl.chiselmon.core.ChiselmonConstants;
import cc.turtl.chiselmon.api.storage.StorageScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

/**
 * Saves/loads a data class as a JSON file within the scope's data directory.
 * <p>
 * Usage:
 * {@code
 * GsonAdapter<FiltersUserData> adapter = GsonAdapter.of(
 *     "filters.json",
 *     FiltersUserData.class,
 *     FiltersUserData::withDefaults
 * );
 * }
 */
public class GsonAdapter<T> implements StorageAdapter<T> {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private final String filename;
    private final Type type;
    private final Supplier<T> defaultFactory;

    private GsonAdapter(String filename, Type type, Supplier<T> defaultFactory) {
        this.filename = filename;
        this.type = type;
        this.defaultFactory = defaultFactory;
    }

    // Single static factory for both Class<T> and Type
    public static <T> GsonAdapter<T> of(String filename, Type type, Supplier<T> defaultFactory) {
        return new GsonAdapter<>(filename, type, defaultFactory);
    }

    @Override
    public T load(StorageScope scope) {
        Path file = scope.dataFile(filename);
        if (!Files.exists(file)) return defaultFactory.get();

        try {
            if (Files.size(file) > 0) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    T result = GSON.fromJson(reader, type);
                    if (result != null) return result;
                }
            }
            handleCorruptedFile(file, "File empty or parsed as null");
        } catch (Exception e) {
            handleCorruptedFile(file, e.getMessage());
        }
        return defaultFactory.get();
    }

    private void handleCorruptedFile(Path file, String reason) {
        ChiselmonConstants.LOGGER.warn("Corrupted data in {}, backing up. Reason: {}", filename, reason);
        try {
            Path backup = file.resolveSibling(filename + ".bak");
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Last resort, if the file can't be backed up, delete it so the next save works
            try { Files.deleteIfExists(file); } catch (IOException ignored) {}
        }
    }

    @Override
    public void save(StorageScope scope, T data) {
        Path file = scope.dataFile(filename);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data, type, writer);
            }
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to save {}: {}", filename, e.getMessage());
        }
    }
}