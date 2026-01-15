package cc.turtl.chiselmon.feature.spawnalert;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.feature.spawnalert.condition.AlertConditionRegistry;
import cc.turtl.chiselmon.feature.spawnalert.condition.CustomListCondition;
import cc.turtl.chiselmon.feature.spawnalert.condition.ExtremeSizeCondition;
import cc.turtl.chiselmon.feature.spawnalert.condition.LegendaryCondition;
import cc.turtl.chiselmon.feature.spawnalert.condition.ShinyCondition;
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

/**
 * Feature that alerts players when notable Pokemon spawn nearby.
 * 
 * <p>This feature has been refactored to follow ECS best practices:
 * <ul>
 *   <li><strong>Conditions:</strong> Use {@link AlertConditionRegistry} for extensible spawn detection</li>
 *   <li><strong>Actions:</strong> Alert behaviors are delegated to AlertAction implementations</li>
 *   <li><strong>Events:</strong> Publishes events for other features to react to</li>
 * </ul>
 * 
 * <h2>Extending Alert Detection:</h2>
 * <p>To add new alert conditions, implement {@link cc.turtl.chiselmon.feature.spawnalert.condition.AlertCondition}
 * and register it with {@link AlertConditionRegistry#register}.
 * 
 * <h2>Architecture:</h2>
 * <pre>
 * SpawnAlertFeature (orchestration)
 *   ├── AlertConditionRegistry (detection strategy)
 *   │     ├── ShinyCondition
 *   │     ├── LegendaryCondition
 *   │     ├── ExtremeSizeCondition
 *   │     └── CustomListCondition
 *   ├── AlertManager (state management)
 *   │     └── AlertAction implementations (behaviors)
 *   └── EventBus (cross-feature communication)
 * </pre>
 */
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
        registerConditions();
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

    /**
     * Register the built-in alert conditions.
     * These are evaluated in order, with the highest matching priority being used.
     */
    private void registerConditions() {
        AlertConditionRegistry.registerAll(
                new ShinyCondition(),
                new ExtremeSizeCondition(),
                new LegendaryCondition(),
                new CustomListCondition());
    }

    private void registerKeybinds() {
        muteAlertsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + ChiselmonConstants.MODID + ".spawnalert.mutealerts",
                GLFW.GLFW_KEY_M,
                ChiselmonConstants.KEYBIND_CATEGORY_KEY));
    }

    private void onClientTickEnd(Minecraft client) {
        if (!canRun()) {
            return;
        }

        handleMuteKeybind();
        trackBattleState();
        alertManager.tick();
    }

    private void handleMuteKeybind() {
        while (muteAlertsKey.consumeClick()) {
            alertManager.muteAllTargets();
            Minecraft.getInstance().player
                    .sendSystemMessage(ComponentUtil.colored(
                            ComponentUtil.modTranslatable("spawnalert.mute.success"), ColorUtil.GREEN));
        }
    }

    private void trackBattleState() {
        ClientBattle currentBattle = CobblemonClient.INSTANCE.getBattle();
        if (currentBattle != null && currentBattle != lastBattle) {
            onBattleStarted(currentBattle);
            lastBattle = currentBattle;
        } else if (currentBattle == null) {
            lastBattle = null;
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
        if (!canRun() || !(entity instanceof PokemonEntity pe)) {
            return;
        }

        SpawnAlertConfig config = getConfig().spawnAlert;

        // Pre-filter: only wild Pokemon
        if (!PokemonEntityPredicates.IS_WILD.test(pe)) {
            return;
        }

        // Pre-filter: suppress plushies (level 1 Pokemon)
        if (config.suppressPlushies && pe.getPokemon().getLevel() == 1) {
            return;
        }

        // Use the condition registry to determine alert priority
        AlertPriority priority = AlertConditionRegistry.evaluate(pe, config);

        if (priority != AlertPriority.NONE) {
            alertManager.addTarget(pe, priority);
        }
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }
}