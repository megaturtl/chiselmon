package cc.turtl.cobbleaid;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.WorldData;

public class WorldDataManager {

    private final Map<String, WorldDataStore> worldDataMap;

    public WorldDataManager(Map<String, WorldDataStore> persistentMap) {
        this.worldDataMap = persistentMap;
    }

    public WorldDataStore getOrCreateStore() {
        return worldDataMap.computeIfAbsent(getWorldIdentifier(), x -> new WorldDataStore());
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
}