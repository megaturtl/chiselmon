package cc.turtl.chiselmon.core.api.storage

import cc.turtl.chiselmon.core.ChiselmonConstants
import com.google.gson.GsonBuilder
import org.h2.jdbcx.JdbcDataSource
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

    fun saveAll() = cache.forEach { (scope, data) -> save(scope, data) }

    fun saveAndClear(scope: Scope) = cache.remove(scope)?.let { data ->
        save(scope, data)
        close(scope, data)
    }
}

private val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()

/** JSON-backed [ScopedStorage]. Writes a pretty-printed file per scope. Corrupted files are backed up automatically. */
fun <T> gsonData(filename: String, type: Type, default: () -> T) = ScopedStorage<T>(
    load = { scope ->
        val file = scope.dataFile(filename)
        if (!Files.exists(file)) return@ScopedStorage default()

        try {
            if (Files.size(file) > 0) {
                GSON.fromJson<T>(Files.newBufferedReader(file), type)
                    ?: run { backupCorrupted(file, filename, "parsed as null"); default() }
            } else {
                backupCorrupted(file, filename, "file is empty"); default()
            }
        } catch (e: Exception) {
            backupCorrupted(file, filename, e.message); default()
        }
    },
    save = { scope, data ->
        val file = scope.dataFile(filename)
        try {
            Files.createDirectories(file.parent)
            Files.newBufferedWriter(file).use { GSON.toJson(data, type, it) }
        } catch (e: IOException) {
            ChiselmonConstants.LOGGER.error("Failed to save {}: {}", filename, e.message)
        }
    }
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
        val ds = JdbcDataSource().apply { setURL("jdbc:h2:$dbPath;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE") }
        try {
            factory(ds.connection, file)
        } catch (e: Exception) {
            throw RuntimeException("Failed to open DB '$filename' for $scope", e)
        }
    },
    save = { _, data -> onSave(data) },
    close = { _, data -> onClose(data) }
)

private fun backupCorrupted(file: Path, filename: String, reason: String?) {
    ChiselmonConstants.LOGGER.warn("Corrupted data in {}, backing up. Reason: {}", filename, reason)
    try {
        Files.move(file, file.resolveSibling("$filename.bak"), StandardCopyOption.REPLACE_EXISTING)
    } catch (e: IOException) {
        try {
            Files.deleteIfExists(file)
        } catch (_: IOException) {
        }
    }
}