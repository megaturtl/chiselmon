package cc.turtl.chiselmon.feature.eggpreview;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;

import cc.turtl.chiselmon.feature.AbstractFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class EggPreviewFeatureFabric extends AbstractFeature {
    private static final EggPreviewFeatureFabric INSTANCE = new EggPreviewFeatureFabric();

    private EggPreviewFeatureFabric() {
        super("Egg Preview");
    }

    public static EggPreviewFeatureFabric getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().eggPreview.enabled;
    }

    @Override
    protected void init() {
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
}