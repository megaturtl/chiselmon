package cc.turtl.chiselmon.platform;

import java.util.ServiceLoader;

/**
 * This class finds and loads service classes that have distinct implementations for different platforms.
 * E.g. PathFinder class can get the path for a mod's data (pathFinder.getPath("cobblemon", "data/cobblemon/species")
 */
public class PlatformHelper {
    private static IPathFinder PATH_FINDER;

    public static IPathFinder getPathFinder() {
        if (PATH_FINDER == null) {
            // This loads the implementation from the platform-specific jar
            PATH_FINDER = ServiceLoader.load(IPathFinder.class)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Failed to find PathFinder service!"));
        }
        return PATH_FINDER;
    }
}
