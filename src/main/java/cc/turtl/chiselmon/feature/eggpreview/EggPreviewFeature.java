package cc.turtl.chiselmon.feature.eggpreview;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class EggPreviewFeature {
    public EggPreviewFeature() {
    }

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
    }

    private void onClientTickEnd(Minecraft client) {
        if (canRun()) {
            if (!getConfig().eggPreview.attemptHatchSync || client.player == null
                    || !(client.screen instanceof PCGUI pcGUI))
                return;

            EggPreviewManager.tick(pcGUI);
        }
    }

    private boolean canRun() {
        return !Chiselmon.isDisabled() && getConfig().eggPreview.enabled;
    }

    private ChiselmonConfig getConfig() {
        return Chiselmon.services().config().get();
    }
}
