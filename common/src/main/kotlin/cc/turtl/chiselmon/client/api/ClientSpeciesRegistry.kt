package cc.turtl.chiselmon.client.api

import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.util.normalizeSpeciesName
import cc.turtl.turtlshell.api.client.ClientEvents
import cc.turtl.turtlshell.api.core.Platform
import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * A lightweight, immutable representation of Pokemon species data for client-side use.
 *
 * All collections are immutable and the name is normalised for consistent lookups.
 */
data class ClientSpecies(
    val pokedexNumber: Int = 0,
    val name: String? = null,
    val catchRate: Int = 0,
    val eggGroups: List<String> = emptyList(),
    val labels: Set<String> = emptySet(),
    val aspects: List<String> = emptyList(),
    val eggCycles: Int = 0,
    val shoulderMountable: Boolean = false,
    val baseStats: Map<String, Int> = emptyMap(),
    val evYield: Map<String, Int> = emptyMap()
)

object ClientSpeciesRegistry {

    private enum class LoadState { IDLE, LOADING, LOADED }

    private val GSON = Gson()
    private var speciesMap: Map<String, ClientSpecies> = emptyMap()
    private var state = LoadState.IDLE

    fun init() {
        ClientEvents.TICK_POST.subscribe {
            if (state == LoadState.IDLE) loadAsync()
        }

        ClientEvents.LEVEL_DISCONNECTED.subscribe {
            speciesMap = emptyMap()
            state = LoadState.IDLE
        }
    }

    private fun loadAsync() {
        state = LoadState.LOADING
        CompletableFuture.runAsync {
            val startTime = System.currentTimeMillis()
            val tempMap = ConcurrentHashMap<String, ClientSpecies>(1024)

            val root: Path? = Platform.findPath("cobblemon", "data/cobblemon/species")
            if (root == null) {
                ChiselmonConstants.LOGGER.warn("Cobblemon species path not found, will retry...")
                state = LoadState.IDLE
                return@runAsync
            }

            try {
                Files.walk(root).use { walk ->
                    walk.parallel()
                        .filter { it.toString().endsWith(".json") }
                        .forEach { parse(it, tempMap) }
                }
                speciesMap = tempMap.toMap()
                state = LoadState.LOADED
                ChiselmonConstants.LOGGER.info(
                    "Indexed {} species in {}ms.",
                    speciesMap.size,
                    System.currentTimeMillis() - startTime
                )
            } catch (e: Exception) {
                ChiselmonConstants.LOGGER.error("Failed indexing species: ", e)
                state = LoadState.IDLE
            }
        }
    }

    private fun parse(path: Path, map: MutableMap<String, ClientSpecies>) {
        try {
            Files.newBufferedReader(path).use { reader ->
                val species = GSON.fromJson(reader, ClientSpecies::class.java) ?: return
                val cleanKey = normalizeSpeciesName(path.fileName.toString().removeSuffix(".json"))
                map[cleanKey] = species
            }
        } catch (_: Exception) {
        }
    }

    @JvmStatic
    fun getSpecies(name: String): ClientSpecies? {
        return name.let { speciesMap[normalizeSpeciesName(it)] }
    }
}