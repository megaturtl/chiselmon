package cc.turtl.chiselmon.feature.checkspawntracker;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class CheckSpawnTrackerFeatureFabric extends cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerFeature {
    public static final CheckSpawnTrackerFeatureFabric INSTANCE = new CheckSpawnTrackerFeatureFabric();

    private CheckSpawnTrackerFeatureFabric() {
        super();
    }

    public static CheckSpawnTrackerFeatureFabric getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        super.init();
        
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!canRun() || overlay) {
                return true;
            }
            boolean captured = this.captureChat(message);
            // if we capture the checkspawn message, don't show it to the user
            return !captured;
        });
    }
}
