package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.api.filter.FiltersUserData
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.core.api.storage.gsonData
import cc.turtl.chiselmon.core.api.storage.h2Data
import cc.turtl.chiselmon.client.feature.pc.PCUserData
import cc.turtl.chiselmon.system.tracker.EncounterDatabase
import cc.turtl.turtlshell.api.client.ClientEvents

/**
 * Central registry for scoped data storage.
 *
 *   val data = ChiselmonStorage.FILTERS[StorageScope.global()]
 */
object ChiselmonStorage {

    @JvmField
    val FILTERS = gsonData("filters.json", FiltersUserData::class.java) { FiltersUserData.withDefaults() }

    @JvmField
    val PC_SETTINGS = gsonData("pc.json", PCUserData::class.java, ::PCUserData)

    @JvmField
    val ENCOUNTERS = h2Data("encounters", ::EncounterDatabase, EncounterDatabase::flush, EncounterDatabase::close)

    private val all = listOf(FILTERS, PC_SETTINGS, ENCOUNTERS)

    private const val AUTOSAVE_INTERVAL_TICKS = 20 * 60 * 5
    private var tickCount = 0

    /** Registers autosave and world-leave cleanup. Call once during mod init. */
    fun init() {
        ClientEvents.LEVEL_DISCONNECTED.subscribe {
            Scope.currentWorld()?.let { world -> all.forEach { it.saveAndClear(world) } }
        }

        ClientEvents.GAME_STOPPING.subscribe {
            saveAll()
        }

        ClientEvents.TICK_POST.subscribe {
            if (++tickCount >= AUTOSAVE_INTERVAL_TICKS) {
                tickCount = 0
                saveAll()
            }
        }
    }

    /** Saves all currently loaded data across all scopes. */
    fun saveAll() = all.forEach { it.saveAll() }
}