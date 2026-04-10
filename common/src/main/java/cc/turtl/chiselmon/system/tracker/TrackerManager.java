package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.core.ChiselmonConstants;
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents;
import cc.turtl.turtlshell.api.client.ClientEvents;
import kotlin.Unit;

public class TrackerManager {
    private static final TrackerManager INSTANCE = new TrackerManager();
    private TrackerSession activeSession;

    private TrackerManager() {
    }

    public static TrackerManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        ClientEvents.INSTANCE.getLEVEL_CONNECTED().subscribe(e -> {
            onWorldJoin();
            return Unit.INSTANCE;
        });
        ClientEvents.INSTANCE.getLEVEL_DISCONNECTED().subscribe(e -> {
            onWorldLeave();
            return Unit.INSTANCE;
        });
        ChiselmonClientEvents.INSTANCE.getPOKEMON_LOADED().subscribe(e -> {
            if (activeSession != null) activeSession.onPokemonLoad(e);
            return Unit.INSTANCE;
        });
        ChiselmonClientEvents.INSTANCE.getPOKEMON_UNLOADED().subscribe(e -> {
            if (activeSession != null) activeSession.onPokemonUnload(e);
            return Unit.INSTANCE;
        });
        ClientEvents.INSTANCE.getTICK_POST().subscribe(e -> {
            if (activeSession != null) activeSession.tick();
            return Unit.INSTANCE;
        });
        ChiselmonConstants.LOGGER.info("TrackerManager initialized");
    }

    private void onWorldJoin() {
        if (activeSession != null) {
            ChiselmonConstants.LOGGER.warn("New world joined before previous TrackerSession was disposed - disposing now");
            activeSession.stopDashboard();
            activeSession = null;
        }
        activeSession = new TrackerSession();
        ChiselmonConstants.LOGGER.debug("TrackerSession created");
    }

    private void onWorldLeave() {
        if (activeSession != null) {
            activeSession.stopDashboard();
            activeSession = null;
            ChiselmonConstants.LOGGER.debug("TrackerSession disposed");
        }
    }

    /**
     * Returns the active tracker for the current world.
     *
     * @throws IllegalStateException if called outside an active world session
     */
    public TrackerSession getTracker() {
        if (activeSession == null) {
            throw new IllegalStateException("Attempted to access PokemonTrackerSystem with no active world.");
        }
        return activeSession;
    }
}