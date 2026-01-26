package cc.turtl.chiselmon.service;

import cc.turtl.chiselmon.feature.pc.tab.PCTabStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.WorldData;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class WorldDataService {
    private final Map<String, WorldDataStore> worldDataMap;
    private final Supplier<String> worldIdentifierSupplier;

    public WorldDataService(Map<String, WorldDataStore> backingStore) {
        this(backingStore, WorldDataService::getWorldIdentifier);
    }

    public WorldDataService(Map<String, WorldDataStore> backingStore, Supplier<String> worldIdentifierSupplier) {
        this.worldDataMap = backingStore != null ? backingStore : new ConcurrentHashMap<>();
        this.worldIdentifierSupplier = worldIdentifierSupplier;
    }

    public static String getWorldIdentifier() {
        Minecraft minecraft = Minecraft.getInstance();
        ServerData server = minecraft.getCurrentServer();

        if (server != null) {
            return "MP:" + server.ip;
        }

        WorldData world = minecraft.getSingleplayerServer().getWorldData();
        if (world != null) {
            return "SP:" + world.getLevelName();
        }

        return "FALLBACK";
    }

    public WorldDataStore current() {
        String worldId;
        try {
            worldId = worldIdentifierSupplier.get();
        } catch (Exception e) {
            worldId = "FALLBACK";
        }
        if (worldId == null) {
            worldId = "FALLBACK";
        }
        return worldDataMap.computeIfAbsent(worldId, x -> new WorldDataStore());
    }

    public Map<String, WorldDataStore> backingStore() {
        return Collections.unmodifiableMap(worldDataMap);
    }

    public class WorldDataStore {
        private final PCTabStore pcTabStore;

        public WorldDataStore() {
            this.pcTabStore = new PCTabStore();
        }

        public PCTabStore getPcTabStore() {
            return pcTabStore;
        }
    }
}
