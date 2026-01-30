package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.event.ClientTickPostHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

// Subscribes common handlers to platform-specific events
public class EventRegisterFabric {
    public static void register() {
        // Bridge to common event handling logic
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickPostHandler::handle);

        // Register commands
        CommandRegistrationCallback.EVENT.register(ChiselmonCommands::register);
    }
}
