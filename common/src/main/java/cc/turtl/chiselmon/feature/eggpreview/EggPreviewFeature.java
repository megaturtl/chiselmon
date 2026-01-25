package cc.turtl.chiselmon.feature.eggpreview;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;

import cc.turtl.chiselmon.feature.AbstractFeature;
import net.minecraft.client.Minecraft;

public class EggPreviewFeature extends AbstractFeature {
    private static final EggPreviewFeature INSTANCE = new EggPreviewFeature();

    protected EggPreviewFeature() {
        super("Egg Preview");
    }

    public static EggPreviewFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().eggPreview.enabled;
    }

    @Override
    protected void init() {
        // Event registration is platform-specific and handled in fabric/neoforge modules
    }

    public void onClientTickEnd(Minecraft client) {
        if (canRun()) {
            if (!getConfig().eggPreview.attemptHatchSync || client.player == null
                    || !(client.screen instanceof PCGUI pcGUI))
                return;

            EggPreviewManager.tick(pcGUI);
        }
    }
}
