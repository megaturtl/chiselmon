package cc.turtl.chiselmon.client.system.spawnrecorder

import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.turtlshell.api.client.ClientEvents

object SpawnRecorderManager {
    var session: SpawnRecorderSession? = null
        private set

    fun init() {
        ClientEvents.LEVEL_CONNECTED.subscribe {
            session
                ?.takeIf { it.isPaused }
                ?.let {
                    it.resume()
                    ChiselmonConstants.LOGGER.info("SpawnRecorder auto-resumed on reconnect")
                }
        }

        ClientEvents.GAME_STOPPING.subscribe { session?.clearActionBar() }

        ChiselmonClientEvents.POKEMON_LOADED.subscribe { e ->
            if (e.isWild) session?.onPokemonLoaded(e.entity)
        }

        ChiselmonClientEvents.POKEMON_UNLOADED.subscribe { e ->
            if (e.isWild) session?.onPokemonUnloaded(e.entity)
        }

        ChiselmonConstants.LOGGER.info("SpawnRecorderManager initialized")
    }

    internal fun onWorldLeave() {
        session?.clearActionBar()
        session
            ?.takeUnless { it.isPaused }
            ?.let {
                it.pause()
                ChiselmonConstants.LOGGER.info("SpawnRecorder auto-paused on disconnect")
            }
    }

    /**
     * Starts a new session against the current TrackerSession. Returns false if globally disabled,
     * a session is already active, or no world is loaded.
     */
    fun startSession(): Boolean {
        if (ChiselmonConfig.general.modDisabled || session != null) return false
        val tracker =
            try {
                TrackerSession.current
            } catch (_: IllegalStateException) {
                return false
            }
        session = SpawnRecorderSession(tracker)
        ChiselmonConstants.LOGGER.info("SpawnRecorder session started")
        return true
    }

    /** Ends the active session and returns it, or null if none was running. */
    fun stopSession(): SpawnRecorderSession? {
        val finished = session ?: return null
        finished.clearActionBar()
        session = null
        ChiselmonConstants.LOGGER.info("SpawnRecorder session stopped")
        return finished
    }
}
