package cc.turtl.chiselmon.feature.spawnalert.experimental;

import java.util.function.Supplier;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.SpawnAlertConfig;
import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Example Fabric adapter that wires spawn alerts into the experimental system.
 * Not registered by default; call {@link #register()} from a feature bootstrap to use.
 */
public final class SpawnAlertAdapter {
    private final SpawnAlertSystem system;
    private final Supplier<SpawnAlertConfig> config;
    private final KeyMapping muteKey;

    public SpawnAlertAdapter(SpawnAlertSystem system, Supplier<SpawnAlertConfig> config) {
        this.system = system;
        this.config = config;
        this.muteKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + ChiselmonConstants.MODID + ".spawnalert.experimental.mutealerts",
                -1,
                ChiselmonConstants.KEYBIND_CATEGORY_KEY));
    }

    public void register() {
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onEntityLoad(net.minecraft.world.entity.Entity entity, net.minecraft.client.multiplayer.ClientLevel level) {
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
            return;
        }
        SpawnAlertConfig cfg = config.get();
        system.onEntitySpawn(new SpawnAlertSystem.EntitySpawned(entity.getUUID(), pokemonEntity), cfg);
    }

    private void onClientTick(Minecraft client) {
        SpawnAlertConfig cfg = config.get();
        if (cfg == null || !cfg.enabled) {
            return;
        }

        while (muteKey.consumeClick()) {
            Component muted = ComponentUtil.colored(
                    ComponentUtil.modTranslatable("spawnalert.mute.success"), ColorUtil.GREEN);
            system.onMute(muted);
        }

        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle != null) {
            ClientBattleActor wildActor = battle.getWildActor();
            if (wildActor != null) {
                system.onBattleStarted(wildActor.getUuid());
            }
        }

        system.tick();
    }
}
