package cc.turtl.chiselmon.core.api.storage

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ScopeTest {
    @TempDir lateinit var root: Path

    private val config: Path
        get() = root.resolve("config")

    @Test
    fun `singleplayer settings stay isolated for names that previously collided`() {
        val firstWorld = Files.createDirectories(root.resolve("A B"))
        val secondWorld = Files.createDirectories(root.resolve("A_B"))
        val first = Scope.singleplayer(firstWorld, config, "A B")
        val second = Scope.singleplayer(secondWorld, config, "A_B")
        val store = fileStore()

        store[first].append("first world")
        store[second].append("second world")
        store.saveAndClear(first)
        store.saveAndClear(second)

        assertTrue(first.dataFile("settings.txt").startsWith(firstWorld))
        assertTrue(second.dataFile("settings.txt").startsWith(secondWorld))
        assertEquals("first world", store[first].toString())
        assertEquals("second world", store[second].toString())
    }

    @Test
    fun `same folder name in different save locations has separate settings`() {
        val first = Scope.singleplayer(root.resolve("first-instance/saves/World"), config, "World")
        val second =
            Scope.singleplayer(root.resolve("second-instance/saves/World"), config, "World")
        val store = fileStore()

        store[first].append("first instance")
        store[second].append("second instance")
        store.saveAndClear(first)
        store.saveAndClear(second)

        assertEquals("first instance", store[first].toString())
        assertEquals("second instance", store[second].toString())
    }

    @Test
    fun `equivalent world paths share one cached value`() {
        val world = root.resolve("saves/World")
        val first = Scope.singleplayer(world, config, "World")
        val equivalent = Scope.singleplayer(world.resolve("."), config, "World")
        val store = fileStore()

        store[first].append("pending edit")

        assertEquals(first, equivalent)
        assertSame(store[first], store[equivalent])
        assertEquals("pending edit", store[equivalent].toString())
    }

    @Test
    fun `moving a world carries persisted settings to its new scope`() {
        val world = Files.createDirectories(root.resolve("Original"))
        val original = Scope.singleplayer(world, config, "Original")
        val store = fileStore()
        store[original].append("saved bookmark")
        store.saveAndClear(original)

        val relocatedWorld = Files.move(world, root.resolve("Renamed"))
        val relocated = Scope.singleplayer(relocatedWorld, config, "Renamed")

        assertNotEquals(original, relocated)
        assertTrue(relocated.dataFile("settings.txt").startsWith(relocatedWorld))
        assertEquals("saved bookmark", store[relocated].toString())
    }

    @Test
    fun `multiplayer addresses that previously collided keep separate persisted settings`() {
        val first = Scope.multiplayer(config, "server.example:25565")
        val second = Scope.multiplayer(config, "server.example_25565")
        val store = fileStore()
        store[first].append("first server")
        store[second].append("second server")
        store.saveAndClear(first)
        store.saveAndClear(second)

        assertTrue(first.dataDir().startsWith(config.resolve("worlds")))
        assertTrue(second.dataDir().startsWith(config.resolve("worlds")))
        assertEquals(
            "first server",
            store[Scope.multiplayer(config, "server.example:25565")].toString(),
        )
        assertEquals(
            "second server",
            store[Scope.multiplayer(config, "server.example_25565")].toString(),
        )
    }

    @Test
    fun `singleplayer imports legacy bookmarks database and backups using the display name`() {
        val legacy = Files.createDirectories(config.resolve("worlds/sp-Display_Name"))
        val bookmarks = """{"bookmarks":{"data":[3]}}"""
        Files.writeString(legacy.resolve("pc.json"), bookmarks)
        Files.writeString(legacy.resolve("pc.json.bak"), "previous bookmarks")
        val legacySource =
            JdbcDataSource().apply { setURL("jdbc:h2:${legacy.resolve("encounters")}") }
        legacySource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE history (species VARCHAR)")
                statement.executeUpdate("INSERT INTO history VALUES ('Pikachu')")
            }
        }
        val originalDatabase = Files.readAllBytes(legacy.resolve("encounters.mv.db"))

        val world = root.resolve("different-save-folder")
        val scope = Scope.singleplayer(world, config, "Display Name")

        assertEquals(bookmarks, Files.readString(scope.dataFile("pc.json")))
        assertEquals("previous bookmarks", Files.readString(scope.dataFile("pc.json.bak")))
        val migratedSource =
            JdbcDataSource().apply { setURL("jdbc:h2:${scope.dataFile("encounters")}") }
        migratedSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT species FROM history").use { rows ->
                    assertTrue(rows.next())
                    assertEquals("Pikachu", rows.getString("species"))
                    assertFalse(rows.next())
                }
            }
        }
        assertEquals(bookmarks, Files.readString(legacy.resolve("pc.json")))
        assertArrayEquals(originalDatabase, Files.readAllBytes(legacy.resolve("encounters.mv.db")))
    }

    @Test
    fun `colliding legacy servers inherit independent copies without modifying the original`() {
        val legacy = Files.createDirectories(config.resolve("worlds/mp-server.example_25565"))
        Files.writeString(legacy.resolve("settings.txt"), "shared legacy settings")
        val store = fileStore()
        val first = Scope.multiplayer(config, "server.example:25565")

        store[first].append(" with a new edit")
        store.saveAndClear(first)
        val second = Scope.multiplayer(config, "server.example_25565")

        assertEquals("shared legacy settings", store[second].toString())
        assertEquals("shared legacy settings with a new edit", store[first].toString())
        assertEquals("shared legacy settings", Files.readString(legacy.resolve("settings.txt")))
    }

    @Test
    fun `existing destination data wins over legacy data and deleted files stay deleted`() {
        val scope = Scope.multiplayer(config, "existing.example")
        Files.createDirectories(scope.dataDir())
        Files.writeString(scope.dataFile("settings.txt"), "newer settings")
        Files.writeString(scope.dataFile("pc.json"), "new bookmarks")
        Files.delete(scope.dataFile("pc.json"))
        val legacy = Files.createDirectories(config.resolve("worlds/mp-existing.example"))
        Files.writeString(legacy.resolve("settings.txt"), "older settings")
        Files.writeString(legacy.resolve("pc.json"), "old bookmarks")

        val reconnected = Scope.multiplayer(config, "existing.example")

        assertEquals("newer settings", fileStore()[reconnected].toString())
        assertFalse(Files.exists(reconnected.dataFile("pc.json")))
        assertEquals("older settings", Files.readString(legacy.resolve("settings.txt")))
        assertEquals("old bookmarks", Files.readString(legacy.resolve("pc.json")))
    }

    @Test
    fun `failed import preserves the source publishes no destination and can be retried`() {
        Files.createDirectories(config.resolve("worlds"))
        val legacy = config.resolve("worlds/sp-Broken")
        Files.writeString(legacy, "unexpected file instead of a directory")
        val world = root.resolve("saves/Broken")

        assertThrows(IOException::class.java) {
            Scope.singleplayer(world, config, "Broken")
        }
        assertEquals("unexpected file instead of a directory", Files.readString(legacy))
        assertFalse(Files.exists(world.resolve("chiselmon")))

        Files.delete(legacy)
        Files.createDirectory(legacy)
        Files.writeString(legacy.resolve("settings.txt"), "recovered legacy data")
        val recovered = Scope.singleplayer(world, config, "Broken")

        assertEquals("recovered legacy data", fileStore()[recovered].toString())
        assertEquals("recovered legacy data", Files.readString(legacy.resolve("settings.txt")))
    }

    private fun fileStore() =
        ScopedStorage(
            load = { scope ->
                val file = scope.dataFile("settings.txt")
                StringBuilder(if (Files.exists(file)) Files.readString(file) else "")
            },
            save = { scope, value ->
                Files.createDirectories(scope.dataDir())
                Files.writeString(scope.dataFile("settings.txt"), value)
            },
        )
}
