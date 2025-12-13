package cc.turtl.cobbleaid.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import cc.turtl.cobbleaid.WorldDataManager;
import cc.turtl.cobbleaid.WorldDataStore;

public class WorldDataService {
    private final Map<String, WorldDataStore> worldDataMap;
    private final Supplier<String> worldIdentifierSupplier;

    public WorldDataService(Map<String, WorldDataStore> backingStore) {
        this(backingStore, WorldDataManager::getWorldIdentifier);
    }

    public WorldDataService(Map<String, WorldDataStore> backingStore, Supplier<String> worldIdentifierSupplier) {
        this.worldDataMap = backingStore != null ? backingStore : new ConcurrentHashMap<>();
        this.worldIdentifierSupplier = worldIdentifierSupplier;
    }

    public WorldDataStore current() {
        return worldDataMap.computeIfAbsent(worldIdentifierSupplier.get(), x -> new WorldDataStore());
    }

    public Map<String, WorldDataStore> backingStore() {
        return Collections.unmodifiableMap(worldDataMap);
    }
}
