package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.event.ClientTickHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class ChiselmonFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Chiselmon.initClient();
        EventRegisterFabric.register();
    }
}
