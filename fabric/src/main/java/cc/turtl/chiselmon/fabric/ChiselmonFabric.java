package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.Chiselmon;
import net.fabricmc.api.ClientModInitializer;

public final class ChiselmonFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Chiselmon.initClient();
        EventRegisterFabric.register();
    }
}
