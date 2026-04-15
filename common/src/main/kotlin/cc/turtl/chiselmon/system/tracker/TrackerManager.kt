package cc.turtl.chiselmon.system.tracker

import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.turtlshell.api.client.ClientEvents

object TrackerManager {

    private var activeSession: TrackerSession? = null

    fun init() {
        ClientEvents.LEVEL_CONNECTED.subscribe { onWorldJoin() }
        ClientEvents.LEVEL_DISCONNECTED.subscribe { onWorldLeave() }

        ChiselmonClientEvents.POKEMON_LOADED.subscribe { activeSession?.onPokemonLoad(it) }
        ChiselmonClientEvents.POKEMON_UNLOADED.subscribe { activeSession?.onPokemonUnload(it) }
        ClientEvents.TICK_POST.subscribe { activeSession?.tick() }

        ChiselmonConstants.LOGGER.info("TrackerManager initialized")
    }

    private fun onWorldJoin() {
        activeSession?.let {
            ChiselmonConstants.LOGGER.warn("New world joined before previous TrackerSession was disposed - disposing now")
            it.stopDashboard()
        }
        activeSession = TrackerSession()
        ChiselmonConstants.LOGGER.debug("TrackerSession created")
    }

    private fun onWorldLeave() {
        activeSession?.let {
            it.stopDashboard()
            activeSession = null
            ChiselmonConstants.LOGGER.debug("TrackerSession disposed")
        }
    }

    /**
     * Returns the active tracker for the current world.
     *
     * @throws IllegalStateException if called outside an active world session
     */
    val tracker: TrackerSession
        get() = activeSession
            ?: error("Attempted to access PokemonTrackerSystem with no active world.")
}
