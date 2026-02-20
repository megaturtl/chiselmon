package cc.turtl.chiselmon.system.spawnrecorder;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.system.tracker.TrackerManager;

public class SpawnRecorderManager {
    private static final SpawnRecorderManager INSTANCE = new SpawnRecorderManager();
    private SpawnRecorderSession activeSession;

    private SpawnRecorderManager() {
    }

    public static SpawnRecorderManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        // Auto-pause/resume around disconnects so elapsed time stays honest
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(Priority.HIGH, e -> {
            if (activeSession != null && !activeSession.isPaused()) {
                activeSession.pause();
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-paused on disconnect");
            }
        });

        ChiselmonEvents.LEVEL_CONNECTED.subscribe(Priority.HIGH, e -> {
            if (activeSession != null && activeSession.isPaused()) {
                activeSession.resume();
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-resumed on reconnect");
            }
        });

        ChiselmonEvents.POKEMON_LOADED.subscribe(Priority.HIGH, e -> {
            if (activeSession != null && e.isWild()) {
                activeSession.onPokemonLoaded(e.entity());
            }
        });

        ChiselmonEvents.POKEMON_UNLOADED.subscribe(Priority.HIGH, e -> {
            if (activeSession != null && e.isWild()) {
                activeSession.onPokemonUnloaded(e.entity());
            }
        });

        ChiselmonEvents.CLIENT_POST_TICK.subscribe(Priority.HIGH, e -> {
            if (activeSession != null) {
                activeSession.tick();
            }
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