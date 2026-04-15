package cc.turtl.chiselmon.system.spawnrecorder

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.util.addGlow
import cc.turtl.chiselmon.client.util.highlightNickname
import cc.turtl.chiselmon.system.tracker.TrackerSession
import cc.turtl.turtlshell.api.core.format.ColorLib
import cc.turtl.turtlshell.api.core.format.formatDuration
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.UUID

class SpawnRecorderSession(private val tracker: TrackerSession) {

    /** Species counts for spawns that occurred while this session was active and unpaused. */
    private val speciesCounts = HashMap<String, Int>()

    /** Tick-age bookkeeping for pokemon entities that have been loaded during this session. */
    private val tickAges = HashMap<UUID, TickData>()

    private var sessionTicks = 0
    var isPaused: Boolean = false
        private set

    fun tick() {
        if (!isPaused) sessionTicks++
        removeOldTickData()

        val config = ChiselmonConfig.recorder
        if (config.actionBar) setActionBarStatus()

        if (config.despawnGlow) {
            tracker.currentlyLoaded.values.forEach { entity ->
                val rgb = if (getTicksLived(entity) >= DESPAWN_MIN_TICKS) {
                    ColorLib.RED.rgb
                } else {
                    LIME_RGB
                }
                entity.addGlow(rgb)
                entity.highlightNickname(rgb)
            }
        }
    }

    private fun removeOldTickData() {
        // Remove UUIDs that haven't been seen lately and are no longer loaded
        tickAges.entries.removeAll { (uuid, data) ->
            uuid !in tracker.currentlyLoaded && sessionTicks - data.sessionTicksAtLastSeen >= EXPIRY_TICKS
        }
    }

    fun onPokemonLoaded(entity: PokemonEntity) {
        if (isPaused) return
        if (entity.uuid in tickAges) return // don't double count
        speciesCounts.merge(entity.pokemon.species.name, 1, Int::plus)
        tickAges.getOrPut(entity.uuid) { TickData() }
    }

    fun onPokemonUnloaded(entity: PokemonEntity) {
        tickAges[entity.uuid]?.let {
            it.accumulatedTicks += entity.ticksLived
            it.sessionTicksAtLastSeen = sessionTicks
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun getTicksLived(entity: PokemonEntity): Int {
        val accumulated = tickAges[entity.uuid]?.accumulatedTicks ?: 0
        return accumulated + entity.ticksLived
    }

    val elapsedMs: Long
        get() = (sessionTicks.toLong() * 1000L) / TICKS_PER_SECOND

    val spawnsPerMinute: Float
        get() = totalRecordedCount.toFloat() / (elapsedMs.toFloat() / 60_000f)

    val currentlyLoadedCount: Int
        get() = tracker.currentlyLoaded.size

    val despawnEligibleCount: Int
        get() = tracker.currentlyLoaded.values.count { getTicksLived(it) >= DESPAWN_MIN_TICKS }

    val totalRecordedCount: Int
        get() = speciesCounts.values.sum()

    fun getTopSpecies(limit: Int): List<Map.Entry<String, Int>> =
        speciesCounts.entries
            .sortedByDescending { it.value }
            .take(limit)

    private fun setActionBarStatus() {
        val loadedCount = currentlyLoadedCount
        val despawnCount = despawnEligibleCount
        val safeCount = loadedCount - despawnCount

        val message: MutableComponent = Component.empty()
            .append(Component.translatable("chiselmon.spawnrecorder.action_bar.loaded").withColor(ColorLib.LIGHT_GRAY.rgb))
            .append(Component.literal(despawnCount.toString()).withColor(ColorLib.RED.rgb))
            .append(Component.literal("/").withColor(ColorLib.DARK_GRAY.rgb))
            .append(Component.literal(safeCount.toString()).withColor(ColorLib.GREEN.rgb))
            .append(Component.literal(" | ").withColor(ColorLib.DARK_GRAY.rgb))
            .append(Component.translatable("chiselmon.spawnrecorder.action_bar.spawns").withColor(ColorLib.LIGHT_GRAY.rgb))
            .append(Component.literal(totalRecordedCount.toString()).withColor(ColorLib.AQUA.rgb))
            .append(Component.literal(" | ").withColor(ColorLib.DARK_GRAY.rgb))
            .append(Component.literal(formatDuration(elapsedMs)).withColor(ColorLib.YELLOW.rgb))

        if (isPaused) {
            message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.paused").withColor(ColorLib.ORANGE.rgb))
        }

        Minecraft.getInstance().gui.setOverlayMessage(message, false)
    }

    private class TickData {
        var accumulatedTicks: Int = 0
        var sessionTicksAtLastSeen: Int = 0
    }

    companion object {
        private const val TICKS_PER_SECOND = 20
        private const val DESPAWN_MIN_TICKS = 600
        private const val EXPIRY_TICKS = 5 * 60 * TICKS_PER_SECOND

        /** "Safe" despawn-glow color -- matches the legacy [ColorUtils.LIME] value. */
        private const val LIME_RGB = 0x32CD32
    }
}
