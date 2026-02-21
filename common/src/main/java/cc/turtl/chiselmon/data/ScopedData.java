package cc.turtl.chiselmon.data;

import cc.turtl.chiselmon.data.storage.ScopeStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds one instance of T per Scope, backed by a ScopeStorage for load/save.
 */
public class ScopedData<T> {

    private final ScopeStorage<T> storage;
    private final Map<Scope, T> instances = new HashMap<>();

    public ScopedData(ScopeStorage<T> storage) {
        this.storage = storage;
    }

    /**
     * Get (or load from disk) the instance for this scope.
     */
    public T get(Scope scope) {
        return instances.computeIfAbsent(scope, storage::load);
    }

    /**
     * Save the instance for this scope if it's loaded.
     */
    public void save(Scope scope) {
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
    public void saveAndClear(Scope scope) {
        T instance = instances.remove(scope);
        if (instance != null) {
            storage.save(scope, instance);
            storage.close(scope, instance);
        }
    }
}