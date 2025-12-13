package cc.turtl.cobbleaid.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.cobbleaid.WorldDataManager;
import cc.turtl.cobbleaid.WorldDataStore;

public class WorldDataService {
    private final Map<String, WorldDataStore> worldDataMap;

    public WorldDataService(Map<String, WorldDataStore> backingStore) {
        this.worldDataMap = backingStore != null ? backingStore : new ConcurrentHashMap<>();
    }

    public WorldDataStore current() {
        return worldDataMap.computeIfAbsent(WorldDataManager.getWorldIdentifier(), x -> new WorldDataStore());
    }

    public Map<String, WorldDataStore> backingStore() {
        return Collections.unmodifiableMap(worldDataMap);
    }
}
