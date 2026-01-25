package cc.turtl.chiselmon.feature.spawnalert;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.minecraft.client.Minecraft;

public class SpawnAlertFeature extends AbstractFeature {
    private static final SpawnAlertFeature INSTANCE = new SpawnAlertFeature();
    protected AlertManager alertManager;

    protected SpawnAlertFeature() {
        super("Spawn Alert");
    }

    public static SpawnAlertFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().spawnAlert.enabled;
    }

    @Override
    protected void init() {
        alertManager = new AlertManager(getConfig().spawnAlert);
        Chiselmon.services().config().addListener(alertManager::onConfigSave);
        // Platform-specific event registration is done in subclass
    }

    public void onClientTickEnd(Minecraft client) {
        if (canRun()) {
            // Platform-specific keybind handling done in subclass
            
            // Run the alert manager
            alertManager.tick();
        }
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    protected void handleMuteKeybind() {
        alertManager.muteAll();
        Minecraft.getInstance().player
                .sendSystemMessage(ComponentUtil.colored(
                        ComponentUtil.modTranslatable("spawnalert.mute.success"), ColorUtil.GREEN));
    }
}
