package cc.turtl.chiselmon.feature.spawnlogger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class SpawnLoggerFeature extends cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerFeature {
    private static final SpawnLoggerFeature INSTANCE = new SpawnLoggerFeature();

    private SpawnLoggerFeature() {
        super();
    }

    public static SpawnLoggerFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        super.init();
        
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnectFabric);
        ClientPlayConnectionEvents.JOIN.register(this::onJoinFabric);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }
    
    private void onDisconnectFabric(ClientPacketListener listener, Minecraft client) {
        onDisconnect();
    }
    
    private void onJoinFabric(ClientPacketListener listener, PacketSender sender, Minecraft client) {
        onJoin();
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