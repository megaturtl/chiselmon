package cc.turtl.chiselmon.event;

import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.services.IPathFinder;
import cc.turtl.chiselmon.services.PlatformHelper;

public class ClientTickHandler {
    public static void handle(boolean isLevelNull) {
        if (isLevelNull || ClientSpeciesRegistry.isLoaded()) return;

        // Automatically gets the right one for the platform!
        IPathFinder finder = PlatformHelper.getPathFinder();
        ClientSpeciesRegistry.loadAsync(finder);
    }
}