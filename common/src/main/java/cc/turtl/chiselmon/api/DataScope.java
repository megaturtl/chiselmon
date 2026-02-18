package cc.turtl.chiselmon.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Defines where persistent data should be stored.
 */
public enum DataScope {
    /**
     * Global data shared across all worlds and servers.
     * Path: chiselmon/data/filename.json
     */
    GLOBAL {
        @Override
        public Path resolvePath(Path baseDir, String fileName) {
            return baseDir.resolve(fileName + ".json");
        }

        @Override
        public boolean shouldClearOnWorldChange() {
            return false;
        }
    },

    /**
     * Per-world (singleplayer) or per-server (multiplayer) data.
     * Path: chiselmon/data/worlds/{worldId}/filename.json
     */
    WORLD {
        @Override
        public Path resolvePath(Path baseDir, String fileName) {
            String worldId = getWorldId();
            return baseDir.resolve("worlds").resolve(worldId).resolve(fileName + ".json");
        }

        @Override
        public boolean shouldClearOnWorldChange() {
            return true;
        }
    };

    /**
     * Gets a unique ID for the current world/server context.
     */
    @NotNull
    public static String getWorldId() {
        Minecraft mc = Minecraft.getInstance();

        // Singleplayer - use world name
        MinecraftServer server = mc.getSingleplayerServer();
        if (server != null) {
            return sanitizeFileName(server.getWorldData().getLevelName());
        }

        // Multiplayer - use server IP
        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) {
            return sanitizeFileName(serverData.ip);
        }

        return "fallback";
    }

    @NotNull
    protected static String sanitizeFileName(@NotNull String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Resolves the full file path for this scope.
     */
    public abstract Path resolvePath(Path baseDir, String fileName);

    /**
     * Whether cached data should be cleared when leaving a world/server.
     */
    public abstract boolean shouldClearOnWorldChange();
}