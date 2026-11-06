package cc.turtl.chiselmon.core.api.storage

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.ChiselmonConstants
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import net.minecraft.client.Minecraft
import net.minecraft.world.level.storage.LevelResource

/**
 * Identifies where scoped data is stored. Either [global] or [currentWorld].
 *
 * val global = Scope.global() val world = Scope.currentWorld() // null if not in a world val file =
 * scope.dataFile("mydata.json")
 */
class Scope
private constructor(
    directory: Path,
    val isGlobal: Boolean = false,
) {
    private val directory = directory.toAbsolutePath().normalize()

    /** The storage directory, or "GLOBAL" for shared configuration. */
    val key: String
        get() = if (isGlobal) "GLOBAL" else directory.toString()

    fun dataDir(): Path = directory

    /** A specific file within this scope's data directory. */
    fun dataFile(filename: String): Path = dataDir().resolve(filename)

    override fun equals(other: Any?) =
        other is Scope && directory == other.directory && isGlobal == other.isGlobal

    override fun hashCode() = 31 * directory.hashCode() + isGlobal.hashCode()

    override fun toString() = "Scope[$key]"

    private fun importLegacy(
        configDirectory: Path,
        legacyKey: String,
    ) {
        val legacy =
            configDirectory
                .resolve("worlds")
                .resolve(legacyKey.replace(legacyUnsafeCharacters, "_"))
        // An existing destination is authoritative; never resurrect deleted files or overwrite
        // newer data.
        if (Files.exists(directory, NOFOLLOW_LINKS) || Files.notExists(legacy, NOFOLLOW_LINKS))
            return

        Files.createDirectories(directory.parent)
        // Retain legacy data for other scopes that collided, and publish only a complete copy.
        val staging = Files.createTempDirectory(directory.parent, ".chiselmon-migration-")
        try {
            Files.walk(legacy).use { paths ->
                paths.forEach { source ->
                    val target = staging.resolve(legacy.relativize(source))
                    when {
                        Files.isSymbolicLink(source) ->
                            throw IOException(
                                "Cannot automatically migrate symbolic link '$source'"
                            )
                        Files.isDirectory(source, NOFOLLOW_LINKS) -> Files.createDirectories(target)
                        else -> Files.copy(source, target)
                    }
                }
            }
            Files.move(staging, directory)
        } catch (failure: Exception) {
            try {
                Files.walk(staging).use { paths ->
                    paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
                }
            } catch (cleanupFailure: Exception) {
                failure.addSuppressed(cleanupFailure)
            }
            throw IOException(
                "Failed to import Chiselmon data from '$legacy' to '$directory'; original data was not modified",
                failure,
            )
        }
    }

    companion object {
        private val legacyUnsafeCharacters = Regex("[^a-zA-Z0-9._\\-]")
        private val globalScope by lazy { Scope(ChiselmonConstants.CONFIG_PATH, isGlobal = true) }
        private var worldScope: Scope? = null

        /** Data lives in config/chiselmon/. Always available. */
        fun global(): Scope = globalScope

        /** Captured on connection so disconnect cleanup does not depend on live Minecraft state. */
        fun currentWorld(): Scope? = worldScope

        internal fun enterWorld() {
            worldScope = null
            val mc = Minecraft.getInstance()
            worldScope =
                mc.singleplayerServer?.let {
                    singleplayer(
                        it.getWorldPath(LevelResource.ROOT),
                        ChiselmonConstants.CONFIG_PATH,
                        it.worldData.levelName,
                    )
                } ?: mc.currentServer?.let { multiplayer(ChiselmonConstants.CONFIG_PATH, it.ip) }
        }

        internal fun leaveWorld(): Scope? = worldScope.also { worldScope = null }

        internal fun singleplayer(
            worldDirectory: Path,
            configDirectory: Path,
            worldName: String,
        ): Scope =
            Scope(worldDirectory.resolve(BuildDetails.MOD_ID)).apply {
                importLegacy(configDirectory, "sp-$worldName")
            }

        internal fun multiplayer(
            configDirectory: Path,
            serverAddress: String,
        ): Scope {
            val digest =
                MessageDigest.getInstance("SHA-256")
                    .digest(serverAddress.toByteArray(Charsets.UTF_8))
            val key = "mp-v2-${HexFormat.of().formatHex(digest)}"
            return Scope(configDirectory.resolve("worlds").resolve(key)).apply {
                importLegacy(configDirectory, "mp-$serverAddress")
            }
        }
    }
}
