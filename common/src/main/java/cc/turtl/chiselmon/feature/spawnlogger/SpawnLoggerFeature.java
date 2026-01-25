package cc.turtl.chiselmon.feature.spawnlogger;

import java.nio.file.Path;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.feature.AbstractFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class SpawnLoggerFeature extends AbstractFeature {
    private static final SpawnLoggerFeature INSTANCE = new SpawnLoggerFeature();
    public static final String EXPORT_COMMAND_PATH = "/" + ChiselmonConstants.MODID + " log export";

    protected SpawnLoggerSession currentSession;
    protected SpawnLoggerSession lastCompletedSession;

    protected SpawnLoggerFeature() {
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
        // Platform-specific event registration is done in subclass
    }

    public void onEntityLoad(Entity entity, ClientLevel level) {
        if (!canRun() || currentSession == null || currentSession.isPaused()) {
            return;
        }

        if (entity instanceof PokemonEntity pokemonEntity && PokemonEntityPredicates.IS_WILD.test(pokemonEntity)) {
            currentSession.log(entity.getUUID(), pokemonEntity);
        }
    }

    public void onClientTick(Minecraft client) {
        if (canRun() && currentSession != null && getConfig().spawnLogger.showActionBarStatus) {
            ActionBarStatus.updateActionBar(currentSession);
        }
    }

    public void onDisconnect() {
        if (currentSession != null && !currentSession.isPaused()) {
            currentSession.pause();
            Chiselmon.getLogger().info("Spawn Logger automatically paused due to disconnect");
        }
    }

    public void onJoin() {
        if (currentSession != null && currentSession.isPaused()) {
            currentSession.resume();
            Chiselmon.getLogger().info("Spawn Logger automatically resumed due to reconnect");
        }
    }

    public void onClientStopping(Minecraft client) {
        if (currentSession != null && getConfig().spawnLogger.autoSaveCsv) {
            try {
                Path exportPath = CsvExporter.exportSession(currentSession);
                Chiselmon.getLogger().info("Auto-saved spawn logger session on game close: " + exportPath.getFileName());

                // Mark as last completed session for potential re-export
                lastCompletedSession = currentSession;
                currentSession = null;
            } catch (Exception e) {
                Chiselmon.getLogger().error("Failed to auto-save spawn log session on game close", e);
            }
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