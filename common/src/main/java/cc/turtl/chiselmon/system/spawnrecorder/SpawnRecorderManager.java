package cc.turtl.chiselmon.system.spawnrecorder;

import cc.turtl.chiselmon.core.ChiselmonConstants;
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents;
import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.turtlshell.api.client.ClientEvents;
import kotlin.Unit;

public class SpawnRecorderManager {
    private static final SpawnRecorderManager INSTANCE = new SpawnRecorderManager();
    private SpawnRecorderSession activeSession;

    private SpawnRecorderManager() {
    }

    public static SpawnRecorderManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        ClientEvents.INSTANCE.getLEVEL_DISCONNECTED().subscribe(e -> {
            if (activeSession != null && !activeSession.isPaused()) {
                activeSession.pause();
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-paused on disconnect");
            }
            return Unit.INSTANCE;
        });

        ClientEvents.INSTANCE.getLEVEL_CONNECTED().subscribe(e -> {
            if (activeSession != null && activeSession.isPaused()) {
                activeSession.resume();
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-resumed on reconnect");
            }
            return Unit.INSTANCE;
        });

        ChiselmonClientEvents.INSTANCE.getPOKEMON_LOADED().subscribe(e -> {
            if (activeSession != null && e.isWild()) {
                activeSession.onPokemonLoaded(e.getEntity());
            }
            return Unit.INSTANCE;
        });

        ChiselmonClientEvents.INSTANCE.getPOKEMON_UNLOADED().subscribe(e -> {
            if (activeSession != null && e.isWild()) {
                activeSession.onPokemonUnloaded(e.getEntity());
            }
            return Unit.INSTANCE;
        });

        ClientEvents.INSTANCE.getTICK_POST().subscribe(e -> {
            if (activeSession != null) {
                activeSession.tick();
            }
            return Unit.INSTANCE;
        });

        ChiselmonConstants.LOGGER.info("SpawnRecorderManager initialized");
    }

    /**
     * Starts a new session against the current TrackerSession.
     * Returns false if a session is already active or no world is loaded.
     */
    public boolean startSession() {
        if (activeSession != null) return false;
        activeSession = new SpawnRecorderSession(TrackerManager.getInstance().getTracker());
        ChiselmonConstants.LOGGER.info("SpawnRecorder session started");
        return true;
    }

    /**
     * Ends the active session and returns it, or null if none was running.
     */
    public SpawnRecorderSession stopSession() {
        if (activeSession == null) return null;
        SpawnRecorderSession finished = activeSession;
        activeSession = null;
        ChiselmonConstants.LOGGER.info("SpawnRecorder session stopped");
        return finished;
    }

    public SpawnRecorderSession getSession() {
        return activeSession;
    }
}