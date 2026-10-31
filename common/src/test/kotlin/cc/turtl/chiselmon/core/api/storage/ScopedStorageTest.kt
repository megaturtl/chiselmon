package cc.turtl.chiselmon.core.api.storage

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class ScopedStorageTest {
    @TempDir
    lateinit var root: Path

    data class Settings(var bookmark: Int = 0, var scale: Double = 1.0)

    private fun newStore() = gsonData("settings.json", Settings::class.java, ::Settings)

    @Test
    fun `serialization failure preserves the saved file and unsaved edits for retry`() {
        val scope = Scope.singleplayer(root.resolve("World"), root.resolve("config"), "World")
        val store = newStore()
        store[scope].bookmark = 7
        store.save(scope)
        val original = Files.readAllBytes(scope.dataFile("settings.json"))
        store[scope].bookmark = 9
        store[scope].scale = Double.NaN

        assertThrows(IOException::class.java) { store.saveAndClear(scope) }

        assertArrayEquals(original, Files.readAllBytes(scope.dataFile("settings.json")))
        assertEquals(7, newStore()[scope].bookmark)
        assertEquals(9, store[scope].bookmark)
        assertTrue(store[scope].scale.isNaN())

        store[scope].scale = 1.5
        store.saveAndClear(scope)

        val reopened = newStore()[scope]
        assertEquals(9, reopened.bookmark)
        assertEquals(1.5, reopened.scale)
    }

    @Test
    fun `failed replacement preserves existing filesystem contents and retries the same edits`() {
        val scope = Scope.singleplayer(root.resolve("World"), root.resolve("config"), "World")
        val store = newStore()
        store[scope].bookmark = 7
        store.save(scope)
        val file = scope.dataFile("settings.json")
        val retained = Files.move(file, scope.dataFile("original.json"))
        val original = Files.readAllBytes(retained)
        Files.createDirectory(file)
        val sentinel = Files.writeString(file.resolve("keep.txt"), "do not remove")
        store[scope].bookmark = 42

        assertThrows(IOException::class.java) { store.saveAndClear(scope) }

        assertEquals("do not remove", Files.readString(sentinel))
        assertArrayEquals(original, Files.readAllBytes(retained))
        assertEquals(42, store[scope].bookmark)

        Files.delete(sentinel)
        Files.delete(file)
        Files.move(retained, file)
        store.saveAndClear(scope)

        assertEquals(42, newStore()[scope].bookmark)
    }

    @Test
    fun `save all reports every failure while saving healthy scopes and retaining failed edits`() {
        val config = root.resolve("config")
        val first = Scope.singleplayer(root.resolve("First"), config, "First")
        val second = Scope.singleplayer(root.resolve("Second"), config, "Second")
        val healthy = Scope.singleplayer(root.resolve("Healthy"), config, "Healthy")
        val store = newStore()
        store[first].apply { bookmark = 11; scale = Double.NaN }
        store[second].apply { bookmark = 22; scale = Double.NaN }
        store[healthy].bookmark = 33

        val failure = assertThrows(IOException::class.java) { store.saveAll() }

        assertEquals(1, failure.suppressed.size)
        assertEquals(33, newStore()[healthy].bookmark)
        assertEquals(11, store[first].bookmark)
        assertEquals(22, store[second].bookmark)

        store[first].scale = 1.0
        store[second].scale = 1.0
        store.saveAll()

        assertEquals(11, newStore()[first].bookmark)
        assertEquals(22, newStore()[second].bookmark)
    }

    @Test
    fun `failed corruption backup preserves both copies and can recover after the conflict is resolved`() {
        val scope = Scope.singleplayer(root.resolve("World"), root.resolve("config"), "World")
        Files.createDirectories(scope.dataDir())
        val file = Files.writeString(scope.dataFile("settings.json"), "{\"bookmark\":")
        val backup = Files.writeString(scope.dataFile("settings.json.bak"), "earlier recovered data")
        val store = newStore()

        assertThrows(IOException::class.java) { store[scope] }

        assertEquals("{\"bookmark\":", Files.readString(file))
        assertEquals("earlier recovered data", Files.readString(backup))

        val olderBackup = Files.move(backup, scope.dataFile("older-backup.json"))
        assertEquals(0, store[scope].bookmark)
        assertFalse(Files.exists(file))
        assertEquals("{\"bookmark\":", Files.readString(backup))
        assertEquals("earlier recovered data", Files.readString(olderBackup))
        store[scope].bookmark = 5
        store.saveAndClear(scope)

        assertEquals(5, newStore()[scope].bookmark)
        assertEquals("{\"bookmark\":", Files.readString(backup))
    }

    @Test
    fun `filesystem read failure is not treated as corrupt JSON or cached as defaults`() {
        val scope = Scope.singleplayer(root.resolve("World"), root.resolve("config"), "World")
        val file = Files.createDirectories(scope.dataFile("settings.json"))
        val sentinel = Files.writeString(file.resolve("keep.txt"), "retain directory contents")
        val store = newStore()

        assertThrows(IOException::class.java) { store[scope] }

        assertEquals("retain directory contents", Files.readString(sentinel))
        assertFalse(Files.exists(scope.dataFile("settings.json.bak")))
        Files.delete(sentinel)
        Files.delete(file)
        Files.writeString(file, "{\"bookmark\":12}")

        assertEquals(12, store[scope].bookmark)
    }
}
