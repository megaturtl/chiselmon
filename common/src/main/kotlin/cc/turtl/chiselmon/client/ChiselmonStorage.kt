package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.client.system.alert.AlertExclusions
import cc.turtl.chiselmon.client.feature.pc.PCUserData
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import cc.turtl.chiselmon.core.api.filter.FiltersUserData
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.core.api.storage.ScopedStorage
import cc.turtl.chiselmon.core.api.storage.gsonData
import cc.turtl.chiselmon.core.api.storage.h2Data

/**
 * Central registry for scoped data storage.
 *
 *   val data = ChiselmonStorage.FILTERS[Scope.global()]
 */
object ChiselmonStorage {
    val FILTERS = gsonData("filters.json", FiltersUserData::class.java) { FiltersUserData.withDefaults() }

    val ALERT_EXCLUSIONS = gsonData("alert-exclusions.json", AlertExclusions::class.java, ::AlertExclusions)

    // @JvmField: accessed from MixinPCGUI.java
    @JvmField
    val PC_SETTINGS = gsonData("pc.json", PCUserData::class.java, ::PCUserData)

    val ENCOUNTERS = h2Data("encounters", ::EncounterDatabase, EncounterDatabase::flush, EncounterDatabase::close)

    private val all = listOf(FILTERS, ALERT_EXCLUSIONS, PC_SETTINGS, ENCOUNTERS)

    private const val AUTOSAVE_INTERVAL_TICKS = 20 * 60 * 5
    private var tickCount = 0

    internal fun tick() {
        if (++tickCount >= AUTOSAVE_INTERVAL_TICKS) {
            tickCount = 0
            saveAll()
        }
    }

    /** Saves all currently loaded data across all scopes. */
    fun saveAll() = persistAll { it.saveAll() }

    internal fun saveAndClear(scope: Scope) = persistAll { it.saveAndClear(scope) }

    internal fun saveAndClearAll() = persistAll { it.saveAndClearAll() }

    private inline fun persistAll(action: (ScopedStorage<*>) -> Unit) {
        all.forEach { storage ->
            try {
                action(storage)
            } catch (failure: Exception) {
                ChiselmonConstants.LOGGER.error("Failed to persist scoped data; cached data retained for retry", failure)
            }
        }
    }
}
