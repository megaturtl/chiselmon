package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;

public class TrackerManager {
    private static final TrackerManager INSTANCE = new TrackerManager();
    private TrackerSession activeSession;

    private TrackerManager() {
    }

    public static TrackerManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        ChiselmonEvents.LEVEL_CONNECTED.subscribe(Priority.HIGH, e -> onWorldJoin());
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(Priority.HIGH, e -> onWorldLeave());
        ChiselmonEvents.POKEMON_LOADED.subscribe(Priority.HIGHEST, e -> {
            if (activeSession != null) activeSession.onPokemonLoad(e);
        });
        ChiselmonEvents.POKEMON_UNLOADED.subscribe(Priority.HIGH, e -> {
            if (activeSession != null) activeSession.onPokemonUnload(e);
        });
        ChiselmonEvents.CLIENT_POST_TICK.subscribe(Priority.HIGHEST, e -> {
            if (activeSession != null) activeSession.tick();
        });
        ChiselmonConstants.LOGGER.info("TrackerManager initialized");
    }

    private void onWorldJoin() {
        if (activeSession != null) {
            ChiselmonConstants.LOGGER.warn("New world joined before previous TrackerSession was disposed - disposing now");
            activeSession = null;
        }
        activeSession = new TrackerSession();
        ChiselmonConstants.LOGGER.debug("TrackerSession created");
    }

    private void onWorldLeave() {
        activeSession = null;
        ChiselmonConstants.LOGGER.debug("TrackerSession disposed");
    }

    /**
     * Returns the active tracker for the current world.
     *
     * @throws IllegalStateException if called outside of an active world session
     */
    public TrackerSession getTracker() {
        if (activeSession == null) {
            throw new IllegalStateException("Attempted to access PokemonTrackerSystem with no active world.");
        }
        return activeSession;
    }
}