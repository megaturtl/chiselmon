package cc.turtl.chiselmon.client.system.tracker

import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.client.api.PokemonLoadedEvent
import cc.turtl.chiselmon.client.api.PokemonUnloadedEvent
import cc.turtl.chiselmon.client.util.removeGlow
import cc.turtl.chiselmon.client.util.resetNickname
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.client.system.dashboard.DashboardServer
import cc.turtl.turtlshell.api.client.ClientEvents
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.*

/**
 * Keeps track of currently loaded WILD pokemon, and stores all new encounters in a database.
 */
class TrackerSession private constructor() {

    private val startTimeMs: Long = System.currentTimeMillis()
    private val seenUuids: MutableSet<UUID> = HashSet()

    val currentlyLoaded: MutableMap<UUID, PokemonEntity> = HashMap()
    val db: EncounterDatabase = run {
        val worldScope = Scope.currentWorld()
            ?: error("TrackerSession must be created while in a world")
        ChiselmonStorage.ENCOUNTERS[worldScope]
    }

    private var dashboardServer: DashboardServer? = null

    fun onPokemonLoad(event: PokemonLoadedEvent) {
        if (!event.isWild) return
        val encounter = event.encounter ?: return

        val uuid = event.entity.uuid
        currentlyLoaded[uuid] = event.entity
        seenUuids.add(uuid)
        db.record(encounter)
    }

    fun onPokemonUnload(event: PokemonUnloadedEvent) {
        currentlyLoaded.remove(event.entity.uuid)
    }

    fun tick() {
        cleanUnloaded()

        // reset these always, before despawn glow or alert, so they always work from a blank slate.
        currentlyLoaded.values.forEach { it.removeGlow(); it.resetNickname() }
    }

    private fun cleanUnloaded() {
        currentlyLoaded.entries.removeAll { it.value.isRemoved }
    }

    fun startDashboard() {
        if (dashboardServer != null) return
        dashboardServer = DashboardServer(db, DASHBOARD_PORT).also { it.start() }
    }

    fun stopDashboard() {
        dashboardServer?.stop()
        dashboardServer = null
    }

    val isDashboardRunning: Boolean
        get() = dashboardServer != null

    fun dashboardUptime(): Long = dashboardServer?.uptime() ?: 0L

    val dashboardPort: Int get() = DASHBOARD_PORT

    val msElapsed: Long
        get() = System.currentTimeMillis() - startTimeMs

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
            get() = activeSession
                ?: error("Attempted to access TrackerSession with no active world.")

        fun init() {
            ClientEvents.LEVEL_CONNECTED.subscribe { start() }
            ClientEvents.LEVEL_DISCONNECTED.subscribe { stop() }

            ChiselmonClientEvents.POKEMON_LOADED.subscribe { activeSession?.onPokemonLoad(it) }
            ChiselmonClientEvents.POKEMON_UNLOADED.subscribe { activeSession?.onPokemonUnload(it) }
            ClientEvents.TICK_POST.subscribe { activeSession?.tick() }

            ChiselmonConstants.LOGGER.info("TrackerSession initialized")
        }

        private fun start() {
            activeSession?.let {
                ChiselmonConstants.LOGGER.warn("New world joined before previous TrackerSession was disposed - disposing now")
                it.stopDashboard()
            }
            activeSession = TrackerSession()
            ChiselmonConstants.LOGGER.debug("TrackerSession created")
        }

        private fun stop() {
            activeSession?.let {
                it.stopDashboard()
                activeSession = null
                ChiselmonConstants.LOGGER.debug("TrackerSession disposed")
            }
        }
    }
}
