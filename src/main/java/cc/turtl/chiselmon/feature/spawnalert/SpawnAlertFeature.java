package cc.turtl.chiselmon.feature.spawnalert;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.ObjectDumper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            alertManager.removeTarget(entity.getUUID());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            alertManager.clearTargets();
        });
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
                alertManager.muteAllTargets();
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

            // Run the alert sound tick logic
            alertManager.tick();
        }
    }

    private void onBattleStarted(ClientBattle battle) {

        ObjectDumper.logObjectFields(Chiselmon.getLogger(), battle);

        ClientBattleActor wildActor = battle.getWildActor();
        if (wildActor != null) {
            UUID uuid = wildActor.getUuid();
            alertManager.muteTargetByActorId(uuid);
        }
    }

    private void onEntityLoad(Entity entity, ClientLevel level) {
        if (canRun() && entity instanceof PokemonEntity pe) {
            AlertPriority priority = getAlertPriority(pe, getConfig().spawnAlert);
            if (priority != AlertPriority.NONE) {
                alertManager.addTarget(pe, priority);
            }
        }
    }

    private AlertPriority getAlertPriority(PokemonEntity pokemonEntity, SpawnAlertConfig config) {
        if (!PokemonEntityPredicates.IS_WILD.test(pokemonEntity)) {
            return AlertPriority.NONE;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();

        // Check shiny and size first - bypasses blacklist
        if ((config.alertOnShiny && PokemonPredicates.IS_SHINY.test(pokemon))) {
            return AlertPriority.SHINY;
        }

        if ((config.alertOnExtremeSize && PokemonPredicates.IS_EXTREME_SIZE.test(pokemon))) {
            return AlertPriority.SIZE;
        }

        // Check legendary types against blacklist
        if ((config.alertOnLegendary
                && (PokemonPredicates.IS_LEGENDARY.test(pokemon) || PokemonPredicates.IS_MYTHICAL.test(pokemon)))
                || (config.alertOnUltraBeast && PokemonPredicates.IS_ULTRABEAST.test(pokemon))
                || (config.alertOnParadox && PokemonPredicates.IS_PARADOX.test(pokemon))) {

            if (!PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
                return AlertPriority.LEGENDARY;
            }
        }

        // Check custom whitelist against blacklist
        if (config.alertOnCustomList && PokemonPredicates.isInCustomList(config.whitelist).test(pokemon)
                && !PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
            return AlertPriority.CUSTOM;
        }

        return AlertPriority.NONE;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }
}