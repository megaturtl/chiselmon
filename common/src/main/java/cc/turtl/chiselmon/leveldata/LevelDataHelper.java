package cc.turtl.chiselmon.leveldata;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;

public class LevelDataHelper {

    /**
     * Gets the persistent level data for the current save/server.
     * Creates a new entry if one doesn't exist.
     */
    public static @NotNull PersistentLevelData getLevelData(@NotNull ClientLevel level) {
        String levelKey = getLevelKey(level);
        return ChiselmonConstants.CONFIG.levelDataMap.computeIfAbsent(levelKey, k -> new PersistentLevelData());
    }

    public static void saveLevelData() {
        ChiselmonConstants.CONFIG_HOLDER.save();
    }

    private static String getLevelKey(ClientLevel level) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.getCurrentServer() != null) {
            String serverAddress = mc.getCurrentServer().ip;
            return "MP: " + serverAddress;
        }

        if (mc.getSingleplayerServer() != null) {
            String worldName = mc.getSingleplayerServer().getWorldData().getLevelName();
            return "SP: " + worldName;
        }

        // Fallback
        return "UNKNOWN";
    }
}