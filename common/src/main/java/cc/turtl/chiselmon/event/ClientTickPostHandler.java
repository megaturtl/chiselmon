package cc.turtl.chiselmon.event;

import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.platform.IPathFinder;
import cc.turtl.chiselmon.platform.PlatformHelper;
import net.minecraft.client.Minecraft;

public class ClientTickPostHandler {
    /**
     * This method runs every time a Client Tick Post event is fired (piped from each platforms event registry)
     */
    public static void handle(Minecraft client) {

        setupClientSpeciesRegistry(client);
    }

    /**
     * Loads basic species data that is normally inaccessible client side (ev yield, catch rate, etc) when the level is loaded.
     */
    private static void setupClientSpeciesRegistry(Minecraft client) {
        if (client.level == null || ClientSpeciesRegistry.isLoaded()) return;

        // Automatically gets the right path finder for the platform
        IPathFinder finder = PlatformHelper.getPathFinder();
        ClientSpeciesRegistry.loadAsync(finder);
    }
}