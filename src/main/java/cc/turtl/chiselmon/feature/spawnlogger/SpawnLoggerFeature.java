package cc.turtl.chiselmon.feature.spawnlogger;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.feature.AbstractFeature;
import cc.turtl.chiselmon.util.CommandUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class SpawnLoggerFeature extends AbstractFeature {
    private static final SpawnLoggerFeature INSTANCE = new SpawnLoggerFeature();
    public static final String EXPORT_COMMAND_PATH = "/" + Chiselmon.MODID + " log export";

    private SpawnLoggerSession currentSession;
    private SpawnLoggerSession lastCompletedSession;

    private SpawnLoggerFeature() {
        super("SpawnLogger");
    }

    public static SpawnLoggerFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().spawnLogger.enabled;
    }

    @Override
    protected void init() {
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onEntityLoad(Entity entity, ClientLevel level) {
        if (!canRun() || currentSession == null || currentSession.isPaused()) {
            return;
        }

        if (entity instanceof PokemonEntity pokemonEntity && PokemonEntityPredicates.IS_WILD.test(pokemonEntity)) {
            currentSession.log(entity.getUUID(), pokemonEntity);
        }
    }

    private void onClientTick(Minecraft client) {
        if (canRun() && currentSession != null && getConfig().spawnLogger.showActionBarStatus) {
            ActionBarStatus.updateActionBar(currentSession);
        }
    }

    public void startSession() {
        if (currentSession == null) {
            currentSession = new SpawnLoggerSession();
        }
    }

    public void finishSession() {
        if (currentSession == null) {
            return;
        }

        lastCompletedSession = currentSession;
        ResultsMessage.sendResultsMessage(currentSession);

        if (getConfig().spawnLogger.autoSaveCsv) {
            CommandUtils.executeClientCommand(EXPORT_COMMAND_PATH);
        }
        currentSession = null;
    }

    public SpawnLoggerSession getSession() {
        return currentSession;
    }

    public SpawnLoggerSession getLastCompletedSession() {
        return lastCompletedSession;
    }
}