package cc.turtl.chiselmon.api.storage.adapter;

import cc.turtl.chiselmon.api.storage.StorageScope;

/**
 * Handles loading and saving a single piece of scoped data.
 *
 * @param <T> the data type
 */
public interface StorageAdapter<T> {

    /**
     * Load data for this scope. Return a fresh default if nothing is saved yet.
     */
    T load(StorageScope scope);

    /**
     * Persist data for this scope to disk.
     */
    void save(StorageScope scope, T data);

    /**
     * Called when a scope is cleared (e.g. world leave).
     * Override if you need to do cleanup (flush + close a DB connection, etc).
     */
    default void close(StorageScope scope, T data) {
    }
}