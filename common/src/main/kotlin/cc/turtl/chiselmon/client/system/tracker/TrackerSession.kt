package cc.turtl.chiselmon.client.system.tracker

import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.client.api.PokemonLoadedEvent
import cc.turtl.chiselmon.client.api.PokemonUnloadedEvent
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.system.dashboard.DashboardServer
import cc.turtl.chiselmon.client.util.removeGlow
import cc.turtl.chiselmon.client.util.resetNickname
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.storage.Scope
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.*

/** Keeps track of currently loaded WILD pokemon, and stores all new encounters in a database. */
class TrackerSession private constructor() {
    private val seenUuids: MutableSet<UUID> = HashSet()

    val currentlyLoaded: MutableMap<UUID, PokemonEntity> = HashMap()
    val db: EncounterDatabase = run {
        val worldScope =
            Scope.currentWorld() ?: error("TrackerSession must be created while in a world")
        ChiselmonStorage.ENCOUNTERS[worldScope]
    }

    private var dashboardServer: DashboardServer? = null
    private var dashboardResumePending = false
    private var wasDisabled = ChiselmonConfig.general.modDisabled

    fun onPokemonLoad(event: PokemonLoadedEvent) {
        if (!event.isWild) return
        val uuid = event.entity.uuid
        currentlyLoaded[uuid] = event.entity
        if (ChiselmonConfig.general.modDisabled) return
        val encounter = event.encounter ?: return
        seenUuids.add(uuid)
        db.record(encounter)
    }

    fun onPokemonUnload(event: PokemonUnloadedEvent) {
        currentlyLoaded.remove(event.entity.uuid)?.let {
            it.removeGlow()
            it.resetNickname()
        }
    }

    fun tick() {
        val disabled = ChiselmonConfig.general.modDisabled
        updateDashboard(disabled)
        cleanUnloaded()

        // Clean the last enabled frame once; disabled ticks must leave other visuals alone.
        if (!disabled || !wasDisabled) {
            currentlyLoaded.values.forEach {
                it.removeGlow()
                it.resetNickname()
            }
        }
        wasDisabled = disabled
    }

    private fun cleanUnloaded() {
        currentlyLoaded.entries.removeAll { (_, entity) ->
            if (!entity.isRemoved) return@removeAll false
            entity.removeGlow()
            entity.resetNickname()
            true
        }
    }

    fun startDashboard(): Boolean {
        if (ChiselmonConfig.general.modDisabled || dashboardServer != null) return false
        dashboardServer = DashboardServer(db, DASHBOARD_PORT).also { it.start() }
        dashboardResumePending = false
        return true
    }

    private fun updateDashboard(disabled: Boolean) {
        if (disabled) {
            dashboardServer?.let {
                it.stop()
                dashboardServer = null
                dashboardResumePending = true
            }
        } else if (dashboardResumePending) {
            dashboardResumePending = false
            try {
                startDashboard()
            } catch (e: Exception) {
                ChiselmonConstants.LOGGER.error("Could not resume Chiselmon Dashboard", e)
            }
        }
    }

    fun stopDashboard() {
        dashboardResumePending = false
        dashboardServer?.stop()
        dashboardServer = null
    }

    val isDashboardRunning: Boolean
        get() = dashboardServer != null

    val isDashboardSuspended: Boolean
        get() = dashboardResumePending

    fun dashboardUptime(): Long = dashboardServer?.uptime() ?: 0L

    val dashboardPort: Int
        get() = DASHBOARD_PORT

    val encounterCount: Int
        get() = seenUuids.size

    companion object {
        private const val DASHBOARD_PORT = 7890

        private var activeSession: TrackerSession? = null

        /**
         * Returns the active tracker for the current world.
         *
         * @throws IllegalStateException if called outside an active world session
         */
        val current: TrackerSession
            get() =
                activeSession ?: error("Attempted to access TrackerSession with no active world.")

        fun init() {
            ChiselmonClientEvents.POKEMON_LOADED.subscribe { activeSession?.onPokemonLoad(it) }
            ChiselmonClientEvents.POKEMON_UNLOADED.subscribe { activeSession?.onPokemonUnload(it) }

            ChiselmonConstants.LOGGER.info("TrackerSession initialized")
        }

        internal fun tick() {
            activeSession?.tick()
        }

        internal fun start() {
            activeSession = TrackerSession()
            ChiselmonConstants.LOGGER.debug("TrackerSession created")
        }

        internal fun stop() {
            activeSession?.let {
                it.stopDashboard()
                it.currentlyLoaded.values.forEach { entity ->
                    entity.removeGlow()
                    entity.resetNickname()
                }
                activeSession = null
                ChiselmonConstants.LOGGER.debug("TrackerSession disposed")
            }
        }
    }
}
