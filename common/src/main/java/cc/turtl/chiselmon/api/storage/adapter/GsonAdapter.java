package cc.turtl.chiselmon.api.storage.adapter;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.storage.StorageScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Saves/loads a data class as a JSON file within the scope's data directory.
 * <p>
 * Usage:
 * ScopeStorage<FiltersUserData> storage = GsonStorage.of(
 * "filters.json",
 * FiltersUserData.class,
 * FiltersUserData::withDefaults
 * );
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

    public static <T> GsonAdapter<T> of(String filename, Class<T> type, Supplier<T> defaultFactory) {
        return new GsonAdapter<>(filename, type, defaultFactory);
    }

    /**
     * Use this overload if you need a generic type token (e.g. TypeToken<List<Foo>>).
     */
    public static <T> GsonAdapter<T> of(String filename, Type type, Supplier<T> defaultFactory) {
        return new GsonAdapter<>(filename, type, defaultFactory);
    }

    @Override
    public T load(StorageScope scope) {
        Path file = scope.dataFile(filename);
        if (!Files.exists(file)) return defaultFactory.get();
        try (Reader reader = Files.newBufferedReader(file)) {
            T result = GSON.fromJson(reader, type);
            return result != null ? result : defaultFactory.get();
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Failed to load {}: {}", file, e.getMessage());
            return defaultFactory.get();
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
            ChiselmonConstants.LOGGER.error("Failed to save {}: {}", file, e.getMessage());
        }
    }
}
