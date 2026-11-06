package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.client.api.PokemonLoadedEvent
import cc.turtl.chiselmon.client.api.PokemonUnloadedEvent
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.config.category.AlertConfig
import cc.turtl.chiselmon.client.config.category.GeneralConfig
import cc.turtl.chiselmon.client.config.category.RecorderConfig
import cc.turtl.chiselmon.client.system.alert.AlertContext
import cc.turtl.chiselmon.client.system.spawnrecorder.SpawnRecorderSession
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.chiselmon.core.api.PokemonEncounter
import cc.turtl.chiselmon.core.api.filter.RuntimeFilter
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.junit.SetupTestDependencies
import cc.turtl.turtlshell.api.core.Priority
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.nio.file.Path
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Isolated

@SetupTestDependencies
@Isolated
class GlobalDisableTest {
    @TempDir lateinit var root: Path

    private lateinit var general: GeneralConfig
    private lateinit var alerts: AlertConfig
    private lateinit var recorder: RecorderConfig

    @BeforeEach
    fun configure() {
        mockkObject(ChiselmonConfig)
        general = GeneralConfig()
        alerts = AlertConfig()
        recorder =
            RecorderConfig().apply {
                actionBar = false
                despawnGlow = false
            }
        every { ChiselmonConfig.general } returns general
        every { ChiselmonConfig.alert } returns alerts
        every { ChiselmonConfig.recorder } returns recorder
    }

    @AfterEach
    fun restoreConfiguration() {
        unmockkObject(ChiselmonConfig)
    }

    @Test
    fun `existing alert contexts stop every action while disabled and become eligible again`() {
        val filter = RuntimeFilter("test", "Test", 0xFF00FF, Priority.NORMAL) { true }
        alerts.masterEnabled = true
        val settings =
            AlertConfig.FilterAlertSettings().apply {
                enabled = true
                sendChatMessage = true
                sendDiscordMessage = true
                playSound = true
                repeatSound = false
                highlightEntity = true
            }
        alerts.filterAlerts[filter.id] = settings
        general.discordWebhookURL = "https://example.invalid/not-a-real-webhook"
        val context = AlertContext(mockk<PokemonEntity>(), listOf(filter), false, alerts)

        assertTrue(context.shouldMessage)
        assertTrue(context.shouldDiscord)
        assertTrue(context.shouldSingleSound)
        assertTrue(context.shouldHighlight)

        general.modDisabled = true

        assertFalse(context.shouldAlert)
        assertFalse(context.shouldMessage)
        assertFalse(context.shouldDiscord)
        assertFalse(context.shouldSingleSound)
        assertFalse(context.shouldHighlight)
        settings.repeatSound = true
        assertFalse(context.shouldRepeatingSound)

        general.modDisabled = false

        assertTrue(context.shouldMessage)
        assertTrue(context.shouldDiscord)
        assertTrue(context.shouldRepeatingSound)
        assertTrue(context.shouldHighlight)
        assertTrue(settings.enabled)
    }

    @Test
    fun `recorder suspension preserves totals elapsed time and manual pause without backfilling`() {
        val loaded = HashMap<UUID, PokemonEntity>()
        val tracker = mockk<TrackerSession>()
        every { tracker.currentlyLoaded } returns loaded
        val session = SpawnRecorderSession(tracker)
        val first = entity(1, "Pikachu")
        loaded[first.uuid] = first
        session.onPokemonLoaded(first)
        repeat(20) { session.tick() }
        assertEquals(1, session.totalRecordedCount)
        assertEquals(1000L, session.elapsedMs)

        general.modDisabled = true
        val disabledSpawn = entity(2, "Eevee")
        loaded[disabledSpawn.uuid] = disabledSpawn
        session.onPokemonLoaded(disabledSpawn)
        repeat(20) { session.tick() }
        assertEquals(1, session.totalRecordedCount)
        assertEquals(1000L, session.elapsedMs)
        assertFalse(session.isPaused)

        session.pause()
        general.modDisabled = false
        repeat(20) { session.tick() }
        assertTrue(session.isPaused)
        assertEquals(1000L, session.elapsedMs)
        assertEquals(1, session.totalRecordedCount)

        session.resume()
        val resumedSpawn = entity(3, "Bulbasaur")
        loaded[resumedSpawn.uuid] = resumedSpawn
        session.onPokemonLoaded(resumedSpawn)
        repeat(20) { session.tick() }

        assertEquals(2000L, session.elapsedMs)
        assertEquals(
            mapOf("Pikachu" to 1, "Bulbasaur" to 1),
            session.getTopSpecies(10).associate { it.key to it.value },
        )
    }

    @Test
    fun `tracker preserves load cleanup while rejecting disabled history and retrospective recording`() {
        val scope = Scope.singleplayer(root.resolve("World"), root.resolve("config"), "World")
        mockkObject(Scope.Companion)
        every { Scope.currentWorld() } returns scope
        var tracker: TrackerSession? = null
        try {
            val constructor =
                TrackerSession::class.java.getDeclaredConstructor().apply { isAccessible = true }
            val active = constructor.newInstance()
            tracker = active
            val first = entity(1, "Pikachu")
            val second = entity(2, "Eevee")
            active.onPokemonLoad(PokemonLoadedEvent(first, encounter(first.uuid), true))
            assertEquals(1, active.encounterCount)
            assertEquals(1, active.db.writeCachedCount)

            general.modDisabled = true
            active.onPokemonLoad(PokemonLoadedEvent(second, encounter(second.uuid), true))
            active.tick()

            assertEquals(setOf(first.uuid, second.uuid), active.currentlyLoaded.keys)
            assertEquals(1, active.encounterCount)
            assertEquals(1, active.db.writeCachedCount)
            active.onPokemonUnload(PokemonUnloadedEvent(first, true))
            assertEquals(setOf(second.uuid), active.currentlyLoaded.keys)

            general.modDisabled = false
            active.tick()
            active.db.flush()
            active.db.connection.createStatement().use { statement ->
                statement.executeQuery("SELECT uuid FROM encounters").use { rows ->
                    assertTrue(rows.next())
                    assertEquals(first.uuid, rows.getObject(1, UUID::class.java))
                    assertFalse(rows.next())
                }
            }
        } finally {
            if (tracker != null) ChiselmonStorage.ENCOUNTERS.saveAndClear(scope)
            unmockkObject(Scope.Companion)
        }
    }

    private fun entity(id: Long, species: String): PokemonEntity = mockk {
        every { uuid } returns UUID(0, id)
        every { pokemon.species.name } returns species
        every { isRemoved } returns false
    }

    private fun encounter(uuid: UUID): PokemonEncounter =
        PokemonEncounter(
            encounteredMs = 1000,
            uuid = uuid,
            species = mockk<Species> { every { name } returns "Pikachu" },
            form = mockk<FormData> { every { name } returns "Normal" },
            level = 5,
            gender = Gender.MALE,
            scale = 1f,
            isShiny = false,
            isLegendary = false,
            dimension = "minecraft:overworld",
            biome = "minecraft:plains",
            dayTime = 0,
            isRaining = false,
            blockName = "minecraft:grass_block",
            spawnedFromSnack = false,
            pokemonX = 0,
            pokemonY = 64,
            pokemonZ = 0,
            playerX = 0,
            playerY = 64,
            playerZ = 0,
        )
}
