package cc.turtl.chiselmon.system.tracker

import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.api.PokemonLoadedEvent
import cc.turtl.chiselmon.client.api.PokemonUnloadedEvent
import cc.turtl.chiselmon.client.util.removeGlow
import cc.turtl.chiselmon.client.util.resetNickname
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.system.dashboard.DashboardServer
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import java.util.UUID

/**
 * Keeps track of currently loaded WILD pokemon, and stores all new encounters in a database.
 */
class TrackerSession {

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

        val uuid = event.entity.uuid
        currentlyLoaded[uuid] = event.entity
        seenUuids.add(uuid)
        db.record(event.encounter)
    }

    fun onPokemonUnload(event: PokemonUnloadedEvent) {
        currentlyLoaded.remove(event.entity.uuid)
    }

    fun tick() {
        cleanUnloaded()

        // reset these always, before despawn glow or alert, so they always work from a blank slate.
        for (entity in currentlyLoaded.values) {
            entity.removeGlow()
            entity.resetNickname()
        }
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
    }
}
