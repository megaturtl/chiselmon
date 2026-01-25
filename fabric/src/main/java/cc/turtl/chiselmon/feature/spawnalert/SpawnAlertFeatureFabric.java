package cc.turtl.chiselmon.feature.spawnalert;

import org.lwjgl.glfw.GLFW;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class SpawnAlertFeatureFabric extends cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature {
    private static final SpawnAlertFeatureFabric INSTANCE = new SpawnAlertFeatureFabric();
    private KeyMapping muteAlertsKey;

    private SpawnAlertFeatureFabric() {
        super();
    }

    public static SpawnAlertFeatureFabric getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        super.init();
        
        registerKeybinds();

        ClientEntityEvents.ENTITY_LOAD.register(alertManager::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(alertManager::onEntityUnload);
        ClientPlayConnectionEvents.DISCONNECT.register(alertManager::onDisconnect);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
    }

    private void registerKeybinds() {
        muteAlertsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + ChiselmonConstants.MODID + ".spawnalert.mutealerts",
                GLFW.GLFW_KEY_M,
                ChiselmonConstants.KEYBIND_CATEGORY_KEY));
    }

    @Override
    public void onClientTickEnd(Minecraft client) {
        if (canRun()) {
            // Handle the mute keybind
            while (muteAlertsKey.consumeClick()) {
                handleMuteKeybind();
            }
        }
        
        super.onClientTickEnd(client);
    }
}
