package cc.turtl.chiselmon.core.api.storage

import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.storage.Scope.Companion.currentWorld
import cc.turtl.chiselmon.core.api.storage.Scope.Companion.global
import net.minecraft.client.Minecraft
import java.nio.file.Path

/**
 * Identifies where scoped data is stored. Either [global] or [currentWorld].
 *
 *   val global = Scope.global()
 *   val world  = Scope.currentWorld()   // null if not in a world
 *   val file   = scope.dataFile("mydata.json")
 */
class Scope private constructor(private val worldKey: String?) {

    val isGlobal: Boolean get() = worldKey == null
    val isWorld: Boolean get() = worldKey != null

    /** Stable string identifier, e.g. "GLOBAL", "sp-My_World", "mp-play.example.com". */
    val key: String get() = worldKey ?: "GLOBAL"

    /**
     * Data directory for this scope.
     * Global: config/chiselmon/ -- World: config/chiselmon/worlds/(key)/
     */
    fun dataDir(): Path = if (isGlobal) {
        ChiselmonConstants.CONFIG_PATH
    } else {
        ChiselmonConstants.CONFIG_PATH.resolve("worlds").resolve(worldKey!!)
    }

    /** A specific file within this scope's data directory. */
    fun dataFile(filename: String): Path = dataDir().resolve(filename)

    override fun equals(other: Any?) = other is Scope && worldKey == other.worldKey
    override fun hashCode() = worldKey.hashCode()
    override fun toString() = if (isGlobal) "Scope[global]" else "Scope[$worldKey]"

    companion object {
        /** Data lives in config/chiselmon/. Always available. */
        fun global(): Scope = Scope(null)

        /** Scope for the current world or server. Null if not in a world. */
        fun currentWorld(): Scope? = resolveWorldKey()?.let { Scope(it) }

        private fun resolveWorldKey(): String? {
            val mc = Minecraft.getInstance()
            mc.singleplayerServer?.let { return sanitize("sp-${it.worldData.levelName}") }
            mc.currentServer?.let { return sanitize("mp-${it.ip}") }
            return null
        }

        private fun sanitize(s: String) = s.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
    }
}