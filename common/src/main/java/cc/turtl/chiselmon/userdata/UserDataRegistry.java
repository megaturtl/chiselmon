package cc.turtl.chiselmon.userdata;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Central registry for all persistent data types.
 */
public class UserDataRegistry {
    private static final Map<Class<?>, ScopedDataStore<?>> stores = new HashMap<>();
    private static Path baseDirectory;
    private static boolean initialized = false;

    /**
     * Initializes the data registry and subscribes to game events.
     */
    public static void init(@NotNull Path configPath) {
        if (initialized) {
            ChiselmonConstants.LOGGER.warn("DataRegistry already initialized");
            return;
        }

        baseDirectory = configPath;

        // Only clear world-scoped data on disconnect
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(e -> saveAndClearWorldData());
        ChiselmonEvents.GAME_STOPPING.subscribe(e -> saveAll());

        initialized = true;
        ChiselmonConstants.LOGGER.info("DataRegistry initialized");
    }

    /**
     * Registers a new data type.
     */
    public static <T> void register(
            @NotNull Class<T> dataClass,
            @NotNull String fileName,
            @NotNull DataScope scope,
            @NotNull Supplier<T> defaultFactory
    ) {
        if (!initialized) {
            throw new IllegalStateException("DataRegistry not initialized. Call init() first.");
        }

        if (stores.containsKey(dataClass)) {
            throw new IllegalArgumentException("Data type already registered: " + dataClass.getName());
        }

        ScopedDataStore<T> store = new ScopedDataStore<>(
                dataClass,
                fileName,
                scope,
                defaultFactory,
                baseDirectory,
                ChiselmonConstants.LOGGER
        );

        stores.put(dataClass, store);
        ChiselmonConstants.LOGGER.debug("Registered data type: {} (scope: {}, file: {})",
                dataClass.getSimpleName(), scope, fileName);
    }

    /**
     * Gets a data store by class.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> ScopedDataStore<T> getStore(@NotNull Class<T> dataClass) {
        ScopedDataStore<?> store = stores.get(dataClass);
        if (store == null) {
            throw new IllegalArgumentException("Data type not registered: " + dataClass.getName());
        }
        return (ScopedDataStore<T>) store;
    }

    /**
     * Gets the current data for a registered type.
     */
    @NotNull
    public static <T> T get(@NotNull Class<T> dataClass) {
        return getStore(dataClass).get();
    }

    /**
     * Saves data for a specific type.
     */
    public static <T> void save(@NotNull Class<T> dataClass) {
        getStore(dataClass).save();
    }

    /**
     * Sets and saves data for a specific type.
     */
    public static <T> void set(@NotNull Class<T> dataClass, @NotNull T data) {
        getStore(dataClass).set(data);
    }

    /**
     * Saves all registered data (both global and world-scoped).
     */
    public static void saveAll() {
        ChiselmonConstants.LOGGER.debug("Saving all persistent data...");
        stores.values().forEach(ScopedDataStore::save);
    }

    /**
     * Saves and clears only world-scoped data (called on level disconnect).
     * Global data remains cached.
     */
    private static void saveAndClearWorldData() {
        ChiselmonConstants.LOGGER.debug("Saving and clearing world-scoped data...");
        stores.values().stream()
                .filter(store -> store.getScope().shouldClearOnWorldChange())
                .forEach(ScopedDataStore::saveAndClear);
    }

    /**
     * Clears cached data for a specific type without saving.
     */
    public static <T> void clear(@NotNull Class<T> dataClass) {
        getStore(dataClass).clear();
    }
}