package cc.turtl.chiselmon.data;

import cc.turtl.chiselmon.api.DataScope;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Manages persistent storage for a specific data type.
 *
 * @param <T> The type of data to store
 */
public class ScopedDataStore<T> {
    private final Class<T> dataClass;
    private final String fileName;
    private final DataScope scope;
    private final Supplier<T> defaultFactory;
    private final Path baseDirectory;
    private final Logger logger;

    private T cachedData;
    private String lastScopeKey;

    public ScopedDataStore(
            @NotNull Class<T> dataClass,
            @NotNull String fileName,
            @NotNull DataScope scope,
            @NotNull Supplier<T> defaultFactory,
            @NotNull Path baseDirectory,
            @NotNull Logger logger
    ) {
        this.dataClass = dataClass;
        this.fileName = fileName;
        this.scope = scope;
        this.defaultFactory = defaultFactory;
        this.baseDirectory = baseDirectory;
        this.logger = logger;
    }

    /**
     * Gets the current data, loading from disk if necessary.
     */
    @NotNull
    public T get() {
        String currentScopeKey = getCurrentScopeKey();

        // Reload if scope changed or not yet loaded
        if (cachedData == null || shouldReload(currentScopeKey)) {
            lastScopeKey = currentScopeKey;
            cachedData = load();
        }

        return cachedData;
    }

    /**
     * Saves the current data to disk.
     */
    public void save() {
        if (cachedData == null) {
            logger.warn("No data to save for {}", fileName);
            return;
        }

        Path file = getFilePath();
        JsonSerializer.save(file, cachedData, logger);
    }

    /**
     * Saves current data and clears the cache.
     */
    public void saveAndClear() {
        save();
        cachedData = null;
        lastScopeKey = null;
    }

    /**
     * Replaces the current data with new data and saves it.
     */
    public void set(@NotNull T newData) {
        cachedData = newData;
        save();
    }

    /**
     * Clears cached data without saving.
     */
    public void clear() {
        cachedData = null;
        lastScopeKey = null;
    }

    @NotNull
    private T load() {
        Path file = getFilePath();
        T loaded = JsonSerializer.load(file, dataClass, logger);

        if (loaded != null) {
            return loaded;
        }

        logger.info("Creating new data for {} (scope: {})", fileName, scope);
        return defaultFactory.get();
    }

    @NotNull
    private Path getFilePath() {
        return scope.resolvePath(baseDirectory, fileName);
    }

    @NotNull
    private String getCurrentScopeKey() {
        return scope.name() + ":" + getFilePath();
    }

    private boolean shouldReload(String currentScopeKey) {
        if (!scope.shouldClearOnWorldChange()) {
            return false;
        }
        return !currentScopeKey.equals(lastScopeKey);
    }

    public DataScope getScope() {
        return scope;
    }

    public String getFileName() {
        return fileName;
    }
}