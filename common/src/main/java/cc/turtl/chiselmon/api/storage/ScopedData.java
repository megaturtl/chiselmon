package cc.turtl.chiselmon.api.storage;

import cc.turtl.chiselmon.api.storage.adapter.StorageAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds one instance of T per Scope, backed by a StorageAdapter for load/save.
 */
public class ScopedData<T> {

    private final StorageAdapter<T> storage;
    private final Map<StorageScope, T> instances = new HashMap<>();

    public ScopedData(StorageAdapter<T> storage) {
        this.storage = storage;
    }

    /**
     * Get (or load from disk) the instance for this scope.
     */
    public T get(StorageScope scope) {
        return instances.computeIfAbsent(scope, storage::load);
    }

    /**      * Save the instance for this scope if it's loaded.
     */
    public void save(StorageScope scope) {
        T instance = instances.get(scope);
        if (instance != null) storage.save(scope, instance);
    }

    /**
     * Save all currently loaded instances (used for autosave / game close).
     */
    public void saveAll() {
        instances.forEach(storage::save);
    }

    /**
     * Save and evict from memory. Calls storage.close() for cleanup (e.g. SQLite).
     */
    public void saveAndClear(StorageScope scope) {
        T instance = instances.remove(scope);
        if (instance != null) {
            storage.save(scope, instance);
            storage.close(scope, instance);
        }
    }
}