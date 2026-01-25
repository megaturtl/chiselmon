package cc.turtl.chiselmon.module.feature;

import org.lwjgl.glfw.GLFW;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.feature.spawnalert.AlertManager;
import cc.turtl.chiselmon.module.ChiselmonModule;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class SpawnAlertModule implements ChiselmonModule {
    public static final String ID = "spawn-alert";
    private AlertManager alertManager;
    private KeyMapping muteAlertsKey;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void initialize() {
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
            while (muteAlertsKey.consumeClick()) {
                alertManager.muteAll();
                Minecraft.getInstance().player
                        .sendSystemMessage(ComponentUtil.colored(
                                ComponentUtil.modTranslatable("spawnalert.mute.success"), ColorUtil.GREEN));
            }

            alertManager.tick();
        }
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    private boolean canRun() {
        return !Chiselmon.isDisabled() && getConfig().spawnAlert.enabled;
    }

    private ChiselmonConfig getConfig() {
        return Chiselmon.services().config().get();
    }
}
