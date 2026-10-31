package cc.turtl.chiselmon.core.api.storage

import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.sql.Connection
import java.sql.SQLException

class H2StorageTest {
    @TempDir
    lateinit var root: Path

    private fun newStore(onSave: (Connection) -> Unit = Connection::commit) =
        h2Data(
            "encounters",
            { connection, _ ->
                connection.createStatement().use { it.execute("CREATE TABLE IF NOT EXISTS history (entry_value INT)") }
                connection.autoCommit = false
                connection
            },
            onSave,
            Connection::close,
        )

    private fun insert(connection: Connection, value: Int) {
        connection.prepareStatement("INSERT INTO history VALUES (?)").use {
            it.setInt(1, value)
            it.executeUpdate()
        }
    }

    private fun value(connection: Connection): Int =
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT entry_value FROM history").use { rows ->
                assertTrue(rows.next())
                val value = rows.getInt(1)
                assertFalse(rows.next())
                value
            }
        }

    private fun assertFileReleased(scope: Scope) {
        FileChannel.open(scope.dataFile("encounters.mv.db"), StandardOpenOption.WRITE).use { channel ->
            channel.tryLock().use { lock ->
                assertNotNull(lock, "Disposed database still holds the file lock")
                assertTrue(lock!!.isValid)
            }
        }
    }

    private fun shutdown(scope: Scope) {
        if (!Files.exists(scope.dataFile("encounters.mv.db"))) return
        val source = JdbcDataSource().apply {
            setURL("jdbc:h2:${scope.dataFile("encounters")};IFEXISTS=TRUE;DB_CLOSE_DELAY=0")
        }
        source.connection.use { connection ->
            connection.createStatement().use { it.execute("SHUTDOWN") }
        }
    }

    @Test
    fun `disposing distinct scopes commits pending rows releases locks and reopens persisted history`() {
        val store = newStore()
        repeat(3) { index ->
            val scope = Scope.singleplayer(root.resolve("World-$index"), root.resolve("config"), "World-$index")
            try {
                val connection = store[scope]
                insert(connection, index)

                store.saveAndClear(scope)

                assertTrue(connection.isClosed)
                assertFileReleased(scope)
                assertEquals(index, value(store[scope]))
                store.saveAndClear(scope)
                assertFileReleased(scope)
            } finally {
                shutdown(scope)
            }
        }
    }

    @Test
    fun `legacy persisted indefinite close delay is reset without losing history`() {
        val scope = Scope.singleplayer(root.resolve("Legacy"), root.resolve("config"), "Legacy")
        Files.createDirectories(scope.dataDir())
        val source = JdbcDataSource().apply { setURL("jdbc:h2:${scope.dataFile("encounters")}") }
        source.connection.use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE history (entry_value INT)")
                it.executeUpdate("INSERT INTO history VALUES (42)")
                it.execute("SET DB_CLOSE_DELAY -1")
                it.execute("SHUTDOWN")
            }
        }
        try {
            val store = newStore()
            assertEquals(42, value(store[scope]))

            store.saveAndClear(scope)

            assertFileReleased(scope)
        } finally {
            shutdown(scope)
        }
    }

    @Test
    fun `failed initialization closes its connection releases the engine and permits retry`() {
        val scope = Scope.singleplayer(root.resolve("Retry"), root.resolve("config"), "Retry")
        var opened: Connection? = null
        var failInitialization = true
        val store = h2Data(
            "encounters",
            { connection, _ ->
                opened = connection
                connection.createStatement().use {
                    it.execute("CREATE TABLE IF NOT EXISTS history (entry_value INT)")
                    if (failInitialization) it.execute("SELECT 1 / 0")
                }
                connection.autoCommit = false
                connection
            },
            Connection::commit,
            Connection::close,
        )
        try {
            assertThrows(RuntimeException::class.java) { store[scope] }

            assertTrue(opened!!.isClosed)
            assertFileReleased(scope)
            failInitialization = false
            insert(store[scope], 7)
            store.saveAndClear(scope)

            assertFileReleased(scope)
            assertEquals(7, value(store[scope]))
            store.saveAndClear(scope)
        } finally {
            shutdown(scope)
        }
    }

    @Test
    fun `shutdown closes healthy scopes while retaining failed transactions for retry`() {
        val config = root.resolve("config")
        val first = Scope.singleplayer(root.resolve("First"), config, "First")
        val second = Scope.singleplayer(root.resolve("Second"), config, "Second")
        val healthy = Scope.singleplayer(root.resolve("Healthy"), config, "Healthy")
        val failedConnections = HashSet<Connection>()
        var failFlush = true
        val store = newStore { connection ->
            if (failFlush && connection in failedConnections) {
                connection.createStatement().use { it.execute("SELECT 1 / 0") }
            }
            connection.commit()
        }
        try {
            val firstConnection = store[first].also { failedConnections.add(it) }
            val secondConnection = store[second].also { failedConnections.add(it) }
            val healthyConnection = store[healthy]
            insert(firstConnection, 11)
            insert(secondConnection, 22)
            insert(healthyConnection, 33)

            val failure = assertThrows(SQLException::class.java) { store.saveAndClearAll() }

            assertEquals(1, failure.suppressed.size)
            assertTrue(healthyConnection.isClosed)
            assertFileReleased(healthy)
            assertFalse(firstConnection.isClosed)
            assertFalse(secondConnection.isClosed)
            assertEquals(11, value(store[first]))
            assertEquals(22, value(store[second]))

            failFlush = false
            store.saveAndClearAll()

            listOf(first, second, healthy).forEach { scope -> assertFileReleased(scope) }
            assertEquals(11, value(store[first]))
            assertEquals(22, value(store[second]))
            assertEquals(33, value(store[healthy]))
            store.saveAndClearAll()
        } finally {
            listOf(first, second, healthy).forEach { shutdown(it) }
        }
    }
}
