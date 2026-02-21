package cc.turtl.chiselmon.data.storage;

import cc.turtl.chiselmon.data.Scope;

/**
 * Handles loading and saving a single piece of scoped data.
 *
 * @param <T> the data type
 */
public interface ScopeStorage<T> {

    /**
     * Load data for this scope. Return a fresh default if nothing is saved yet.
     */
    T load(Scope scope);

    /**
     * Persist data for this scope to disk.
     */
    void save(Scope scope, T data);

    /**
     * Called when a scope is cleared (e.g. world leave).
     * Override if you need to do cleanup (flush + close a DB connection, etc).
     */
    default void close(Scope scope, T data) {
    }
}