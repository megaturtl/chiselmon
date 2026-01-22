package cc.turtl.chiselmon.feature.spawnalert;

import org.lwjgl.glfw.GLFW;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class SpawnAlertFeature extends AbstractFeature {
    private static final SpawnAlertFeature INSTANCE = new SpawnAlertFeature();
    private AlertManager alertManager;
    private KeyMapping muteAlertsKey;

    private SpawnAlertFeature() {
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

        registerKeybinds();

        alertManager = new AlertManager(getConfig().spawnAlert);

        ClientEntityEvents.ENTITY_LOAD.register(alertManager::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(alertManager::onEntityUnload);
        ClientPlayConnectionEvents.DISCONNECT.register(alertManager::onDisconnect);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);

        Chiselmon.services().config().addListener(alertManager::onConfigSave);
    }

    private void registerKeybinds() {
        muteAlertsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + ChiselmonConstants.MODID + ".spawnalert.mutealerts",
                GLFW.GLFW_KEY_M,
                ChiselmonConstants.KEYBIND_CATEGORY_KEY));
    }

    private void onClientTickEnd(Minecraft client) {
        if (canRun()) {

            // Handle the mute keybind
            while (muteAlertsKey.consumeClick()) {
                alertManager.muteAll();
                Minecraft.getInstance().player
                        .sendSystemMessage(ComponentUtil.colored(
                                ComponentUtil.modTranslatable("spawnalert.mute.success"), ColorUtil.GREEN));
            }

            // Run the alert manager
            alertManager.tick();
        }
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }
}