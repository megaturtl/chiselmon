package cc.turtl.chiselmon.data;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.DataScope;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Static access point for reading and writing persistent user data.
*/
public final class UserDataManager {
    private static final Logger LOGGER = ChiselmonConstants.LOGGER;

    private UserDataManager() {}

    /**
     * Saves an object to disk as JSON under the given scope.
     *
     * @param scope    which scope to store the file (global, world)
     * @param fileName logical file name, no extension
     * @param data     any Gson-serializable object
     * @return true if saved successfully
     */
    public static boolean save(DataScope scope, String fileName, Object data) {
        Path path = resolvePath(scope, fileName);
        return JsonSerializer.save(path, data, LOGGER);
    }

    /**
     * Loads a simple (non-generic) type from disk.
     *
     * @return deserialized object, or null if the file doesn't exist or is unreadable
     */
    @Nullable
    public static <T> T load(DataScope scope, String fileName, Class<T> type) {
        Path path = resolvePath(scope, fileName);
        return JsonSerializer.load(path, type, LOGGER);
    }

    public static boolean exists(DataScope scope, String fileName) {
        return resolvePath(scope, fileName).toFile().exists();
    }

    /**
     * Deletes the data file for the given scope and name, if it exists.
     *
     * @return true if deleted, false if it didn't exist or deletion failed
     */
    public static boolean delete(DataScope scope, String fileName) {
        try {
            return Files.deleteIfExists(resolvePath(scope, fileName));
        } catch (IOException e) {
            LOGGER.error("Failed to delete data file: {}", fileName, e);
            return false;
        }
    }

    private static Path resolvePath(DataScope scope, String fileName) {
        return scope.resolvePath(ChiselmonConstants.CONFIG_PATH, fileName);
    }
}