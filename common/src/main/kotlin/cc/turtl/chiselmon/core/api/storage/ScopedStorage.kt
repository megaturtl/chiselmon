package cc.turtl.chiselmon.core.api.storage

import cc.turtl.chiselmon.BuildDetails
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import org.apache.logging.log4j.LogManager
import org.h2.jdbcx.JdbcDataSource
import java.io.EOFException
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection

/**
 * Holds one lazily loaded instance of T per [Scope].
 * Create via [gsonData] or [h2Data].
 *
 *   val data = myStore[scope]      // loads on first access
 *   myStore.save(scope)
 *   myStore.saveAndClear(scope)    // persist + evict (call on world leave)
 */
class ScopedStorage<T>(
    private val load: (Scope) -> T,
    private val save: (Scope, T) -> Unit,
    private val close: (Scope, T) -> Unit = { _, _ -> },
) {
    private val cache = HashMap<Scope, T>()

    operator fun get(scope: Scope): T = cache.getOrPut(scope) { load(scope) }

    fun save(scope: Scope) = cache[scope]?.let { save(scope, it) }

    fun saveAll() = persistAll(clear = false)

    fun saveAndClearAll() = persistAll(clear = true)

    private fun persistAll(clear: Boolean) {
        var failure: Exception? = null
        val entries = cache.entries.iterator()
        while (entries.hasNext()) {
            val (scope, data) = entries.next()
            try {
                save(scope, data)
                if (clear) {
                    close(scope, data)
                    entries.remove()
                }
            } catch (error: Exception) {
                val previous = failure
                if (previous == null) failure = error
            }
        }
        failure?.let { throw it }
    }

    fun saveAndClear(scope: Scope) {
        val data = cache[scope] ?: return
        save(scope, data)
        close(scope, data)
        cache.remove(scope)
    }
}

private val LOGGER = LogManager.getLogger(BuildDetails.MOD_ID)
private val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()

/** JSON-backed [ScopedStorage]. Writes a pretty-printed file per scope. Corrupted files are backed up automatically. */
fun <T> gsonData(
    filename: String,
    type: Type,
    default: () -> T,
) = ScopedStorage<T>(
    load = { scope ->
        val file = scope.dataFile(filename)
        if (Files.notExists(file)) return@ScopedStorage default()

        val data =
            try {
                Files.newBufferedReader(file).use { reader -> GSON.fromJson<T>(reader, type) }
            } catch (error: JsonSyntaxException) {
                val cause = error.cause
                if (cause is IOException && cause !is EOFException && cause !is MalformedJsonException) throw cause
                backupCorrupted(file, filename, error.message)
                return@ScopedStorage default()
            }
        data ?: run {
            backupCorrupted(file, filename, "parsed as null or empty")
            default()
        }
    },
    save = { scope, data ->
        val file = scope.dataFile(filename)
        Files.createDirectories(file.parent)
        val temporary = Files.createTempFile(file.parent, ".$filename-", ".tmp")
        try {
            Files.newBufferedWriter(temporary).use { GSON.toJson(data, type, it) }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (failure: Exception) {
            try {
                Files.deleteIfExists(temporary)
            } catch (cleanupFailure: Exception) {
                failure.addSuppressed(cleanupFailure)
            }
            throw IOException("Failed to save '$file'", failure)
        }
    },
)

/**
 * H2-backed [ScopedStorage]. Opens one embedded database file per scope.
 * [onSave] can be omitted for databases that write in real time.
 */
fun <T> h2Data(
    filename: String,
    factory: (Connection, Path) -> T,
    onSave: (T) -> Unit = {},
    onClose: (T) -> Unit,
) = ScopedStorage<T>(
    load = { scope ->
        val file = scope.dataFile(filename)
        Files.createDirectories(file.parent)
        val dbPath = file.toAbsolutePath().toString().removeSuffix(".db")
        // H2 persists this setting, so override the indefinite delay in existing databases too.
        val ds = JdbcDataSource().apply { setURL("jdbc:h2:$dbPath;DB_CLOSE_DELAY=0") }
        var connection: Connection? = null
        try {
            val opened = ds.connection
            connection = opened
            factory(opened, file)
        } catch (failure: Throwable) {
            try {
                connection?.close()
            } catch (closeFailure: Throwable) {
                if (closeFailure !== failure) failure.addSuppressed(closeFailure)
            }
            if (failure is Exception) throw RuntimeException("Failed to open DB '$filename' for $scope", failure)
            throw failure
        }
    },
    save = { _, data -> onSave(data) },
    close = { _, data -> onClose(data) },
)

private fun backupCorrupted(
    file: Path,
    filename: String,
    reason: String?,
) {
    LOGGER.warn("Corrupted data in {}, backing up. Reason: {}", filename, reason)
    Files.move(file, file.resolveSibling("$filename.bak"))
}
