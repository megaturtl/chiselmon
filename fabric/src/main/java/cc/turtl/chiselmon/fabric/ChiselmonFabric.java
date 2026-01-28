package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.event.ClientTickHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class ChiselmonFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        Chiselmon.initClient();

        // Register events to the common handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Bridge to common logic
            ClientTickHandler.handle(client.level == null);
        });
    }
}
