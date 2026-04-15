package cc.turtl.chiselmon.client.system.spawnrecorder

import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.client.system.tracker.TrackerManager
import cc.turtl.turtlshell.api.client.ClientEvents

object SpawnRecorderManager {

    var session: SpawnRecorderSession? = null
        private set

    fun init() {
        ClientEvents.LEVEL_DISCONNECTED.subscribe {
            session?.takeUnless { it.isPaused }?.let {
                it.pause()
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-paused on disconnect")
            }
        }

        ClientEvents.LEVEL_CONNECTED.subscribe {
            session?.takeIf { it.isPaused }?.let {
                it.resume()
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-resumed on reconnect")
            }
        }

        ChiselmonClientEvents.POKEMON_LOADED.subscribe { e ->
            if (e.isWild) session?.onPokemonLoaded(e.entity)
        }

        ChiselmonClientEvents.POKEMON_UNLOADED.subscribe { e ->
            if (e.isWild) session?.onPokemonUnloaded(e.entity)
        }

        ClientEvents.TICK_POST.subscribe { session?.tick() }

        ChiselmonConstants.LOGGER.info("SpawnRecorderManager initialized")
    }

    /**
     * Starts a new session against the current TrackerSession.
     * Returns false if a session is already active or no world is loaded.
     */
    fun startSession(): Boolean {
        if (session != null) return false
        session = SpawnRecorderSession(TrackerManager.tracker)
        ChiselmonConstants.LOGGER.info("SpawnRecorder session started")
        return true
    }

    /** Ends the active session and returns it, or null if none was running. */
    fun stopSession(): SpawnRecorderSession? {
        val finished = session ?: return null
        session = null
        ChiselmonConstants.LOGGER.info("SpawnRecorder session stopped")
        return finished
    }
}
