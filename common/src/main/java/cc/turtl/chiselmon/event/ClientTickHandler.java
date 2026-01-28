package cc.turtl.chiselmon.event;

import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.services.IPathFinder;
import cc.turtl.chiselmon.services.PlatformHelper;
import net.minecraft.client.Minecraft;

public class ClientTickHandler {
    public static void handle(Minecraft client) {
        if (client.level == null || ClientSpeciesRegistry.isLoaded()) return;

        // Automatically gets the right one for the platform!
        IPathFinder finder = PlatformHelper.getPathFinder();
        ClientSpeciesRegistry.loadAsync(finder);
    }
}