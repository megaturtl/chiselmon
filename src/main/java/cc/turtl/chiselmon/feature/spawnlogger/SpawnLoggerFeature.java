package cc.turtl.chiselmon.feature.spawnlogger;

import java.nio.file.Path;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.util.CommandUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;

public final class SpawnLoggerFeature {
    public static final String EXPORT_COMMAND_PATH = "/" + ChiselmonConstants.MODID + " log export";

    private SpawnLoggerSession currentSession;
    private SpawnLoggerSession lastCompletedSession;

    public SpawnLoggerFeature() {
    }

    public void initialize() {
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
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

    private void onDisconnect(ClientPacketListener listener, Minecraft client) {
        if (currentSession != null && !currentSession.isPaused()) {
            currentSession.pause();
            Chiselmon.getLogger().info("Spawn Logger automatically paused due to disconnect");
        }
    }

    private void onJoin(ClientPacketListener listener, PacketSender sender, Minecraft client) {
        if (currentSession != null && currentSession.isPaused()) {
            currentSession.resume();
            Chiselmon.getLogger().info("Spawn Logger automatically resumed due to reconnect");
        }
    }

    private void onClientStopping(Minecraft client) {
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

    private boolean canRun() {
        return !Chiselmon.isDisabled() && getConfig().spawnLogger.enabled;
    }

    private ChiselmonConfig getConfig() {
        return Chiselmon.services().config().get();
    }
}
