package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class ChiselmonFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventRegisterFabric.register();
        ChiselmonKeybinds.ALL.forEach(KeyBindingHelper::registerKeyBinding);
        Chiselmon.initClient();
    }
}
