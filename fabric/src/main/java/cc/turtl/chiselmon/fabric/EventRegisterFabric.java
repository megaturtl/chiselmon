package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.platform.PlatformEventHandlers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

// Subscribes common handlers to platform-specific events
public class EventRegisterFabric {
    public static void register() {
        // Bridge to common event handling logic
        ClientTickEvents.END_CLIENT_TICK.register(PlatformEventHandlers::handleClientPostTick);

        ClientEntityEvents.ENTITY_LOAD.register(PlatformEventHandlers::handleEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(PlatformEventHandlers::handleEntityUnload);

        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, sender, minecraft) -> PlatformEventHandlers.handleLevelConnect());
        ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, minecraft) -> PlatformEventHandlers.handleLevelDisconnect());

        // Register commands
        CommandRegistrationCallback.EVENT.register(ChiselmonCommands::register);
    }
}
