package cc.turtl.chiselmon.api.storage;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Client-side scope. Either Global or World.
 * <p>
 * GLOBAL - persists across everything. One folder, always available.
 * Use for cross-server/cross-world settings and data.
 * <p>
 * WORLD  - unique to the current singleplayer world OR multiplayer server.
 * Only available while the client is connected/in a world.
 * Singleplayer: identified by world folder name.
 * Multiplayer:  identified by server address.
 * <p>
 * Usage:
 * Scope global = Scope.global();
 * Scope world  = Scope.currentWorld();         // null if not in a world
 * <p>
 * Path dir  = scope.dataDir();
 * Path file = scope.dataFile("mydata.json");
 */
public final class StorageScope {

    private final String worldKey; // null = global

    private StorageScope(String worldKey) {
        this.worldKey = worldKey;
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    /**
     * Always available. Data lives in config/chiselmon/
     */
    public static StorageScope global() {
        return new StorageScope(null);
    }

    /**
     * Returns a scope for the current world/server the client is in.
     * Returns null if the client is not currently in a world.
     */
    @Nullable
    public static StorageScope currentWorld() {
        String key = resolveWorldKey();
        return key != null ? new StorageScope(key) : null;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /**
     * Gets a unique ID for the current world/server context.
     */
    private static String resolveWorldKey() {
        Minecraft mc = Minecraft.getInstance();

        MinecraftServer server = mc.getSingleplayerServer();
        if (server != null) {
            return sanitize("sp-" + server.getWorldData().getLevelName());
        }

        // Multiplayer - use server IP
        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) {
            return sanitize("mp-" + serverData.ip);
        }

        return null;
    }

    /**
     * Strip characters unsafe in file paths.
     */
    private static String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    public boolean isGlobal() {
        return worldKey == null;
    }

    // ── File paths ────────────────────────────────────────────────────────────

    public boolean isWorld() {
        return worldKey != null;
    }

    /**
     * String identifier for the current world/server.
     * e.g. "sp-My_World" or "mp-play.example.com"
     * Null for global scope.
     */
    @Nullable
    public String worldKey() {
        return worldKey;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Data directory for this scope.
     * <p>
     * Global:       .mc/config/chiselmon/
     * Singleplayer: .mc/config/chiselmon/worlds/sp-<worldname>/
     * Multiplayer:  .mc/config/chiselmon/worlds/mp-<serveraddress>/
     */
    public Path dataDir() {
        Path base = ChiselmonConstants.CONFIG_PATH;
        if (isGlobal()) {
            return base;
        } else {
            // worldKey is like "sp-My_World" or "mp-play.example.com"
            return base.resolve("worlds").resolve(worldKey);
        }
    }

    /**
     * A specific file within this scope's data directory.
     */
    public Path dataFile(String filename) {
        return dataDir().resolve(filename);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageScope other)) return false;
        return java.util.Objects.equals(worldKey, other.worldKey);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(worldKey);
    }

    @Override
    public String toString() {
        return isGlobal() ? "Scope[global]" : "Scope[" + worldKey + "]";
    }
}