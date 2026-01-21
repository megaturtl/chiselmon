package cc.turtl.chiselmon.feature.spawnalert;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;

public final class SpawnAlertFeature extends AbstractFeature {
    private static final SpawnAlertFeature INSTANCE = new SpawnAlertFeature();
    private AlertManager alertManager;
    private KeyMapping muteAlertsKey;
    private ClientBattle lastBattle = null;

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

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd);
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
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

            // Track new battles starting
            ClientBattle currentBattle = CobblemonClient.INSTANCE.getBattle();
            if (currentBattle != null && currentBattle != lastBattle) {
                onBattleStarted(currentBattle);
                lastBattle = currentBattle;
            } else if (currentBattle == null) {
                lastBattle = null;
            }

            // Run the alert manager
            alertManager.tick();
        }
    }

    private void onBattleStarted(ClientBattle battle) {
        ClientBattleActor wildActor = battle.getWildActor();
        if (wildActor != null) {
            UUID uuid = wildActor.getUuid();
            alertManager.muteLoadedByActorId(uuid);
        }
    }

    private void onEntityLoad(Entity entity, ClientLevel level) {
        if (canRun() && entity instanceof PokemonEntity pe) {
            alertManager.onEntityLoad(pe);
        }
    }

    private void onEntityUnload(Entity entity, ClientLevel level) {
        if (entity instanceof PokemonEntity pe) {
            alertManager.onEntityUnload(pe);
        }
    }

    private void onDisconnect(ClientPacketListener handler, Minecraft client) {
            alertManager.clearLoaded();
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }
}