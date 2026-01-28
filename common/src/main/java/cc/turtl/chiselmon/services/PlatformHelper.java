package cc.turtl.chiselmon.services;

import java.util.ServiceLoader;

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
