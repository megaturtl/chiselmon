package cc.turtl.chiselmon.system.tracker

import cc.turtl.chiselmon.core.api.PokemonEncounter
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.SQLException
import java.util.UUID

class EncounterDatabase(private val conn: Connection, val dbPath: Path) {

    private val writeCache = LinkedHashMap<UUID, PokemonEncounter>()

    init {
        try {
            conn.autoCommit = false
            initSchema()
        } catch (e: SQLException) {
            throw RuntimeException("Failed to init encounter DB schema", e)
        }
    }

    private fun initSchema() {
        conn.createStatement().use { s ->
            s.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS encounters (
                    uuid               UUID         PRIMARY KEY,
                    species            VARCHAR(64)  NOT NULL,
                    form               VARCHAR(64),
                    level              INT          NOT NULL,
                    gender             VARCHAR(16)  NOT NULL,
                    scale_modifier     FLOAT        NOT NULL,
                    is_shiny           BOOLEAN      NOT NULL,
                    is_legendary       BOOLEAN      NOT NULL,
                    dimension          VARCHAR(128) NOT NULL,
                    biome              VARCHAR(128) NOT NULL,
                    world_time         BIGINT       NOT NULL,
                    is_raining         BOOLEAN      NOT NULL,
                    block_name         VARCHAR(128) NOT NULL,
                    from_snack         BOOLEAN      NOT NULL,
                    pokemon_x          INT          NOT NULL,
                    pokemon_y          INT          NOT NULL,
                    pokemon_z          INT          NOT NULL,
                    player_x           INT          NOT NULL,
                    player_y           INT          NOT NULL,
                    player_z           INT          NOT NULL,
                    encountered_ms  BIGINT       NOT NULL,
                    encountered_time     TIMESTAMP AS DATEADD('MILLISECOND', encountered_ms, TIMESTAMP '1970-01-01 00:00:00')
                )
                """.trimIndent()
            )
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_species    ON encounters(species)")
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_timestamp  ON encounters(encountered_ms)")
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_shiny      ON encounters(is_shiny)")
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_legendary  ON encounters(is_legendary)")
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_dimension  ON encounters(dimension)")
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_biome      ON encounters(biome)")
            conn.commit()
        }
    }

    fun record(encounter: PokemonEncounter) {
        writeCache[encounter.uuid] = encounter
        if (writeCache.size >= FLUSH_THRESHOLD) flush()
    }

    fun flush() {
        if (writeCache.isEmpty()) return

        val sql = """
            MERGE INTO encounters
                (uuid, species, form, level, gender, scale_modifier, is_shiny, is_legendary,
                 dimension, biome, world_time, is_raining, block_name, from_snack,
                 pokemon_x, pokemon_y, pokemon_z, player_x, player_y, player_z, encountered_ms)
            KEY(uuid)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            conn.prepareStatement(sql).use { ps ->
                for (e in writeCache.values) {
                    ps.setObject(1, e.uuid)
                    ps.setString(2, e.species.name)
                    ps.setString(3, e.form.name)
                    ps.setInt(4, e.level)
                    ps.setString(5, e.gender.name)
                    ps.setFloat(6, e.scale)
                    ps.setBoolean(7, e.isShiny)
                    ps.setBoolean(8, e.isLegendary)
                    ps.setString(9, e.dimension)
                    ps.setString(10, e.biome)
                    ps.setLong(11, e.dayTime)
                    ps.setBoolean(12, e.isRaining)
                    ps.setString(13, e.blockName)
                    ps.setBoolean(14, e.spawnedFromSnack)
                    ps.setInt(15, e.pokemonX)
                    ps.setInt(16, e.pokemonY)
                    ps.setInt(17, e.pokemonZ)
                    ps.setInt(18, e.playerX)
                    ps.setInt(19, e.playerY)
                    ps.setInt(20, e.playerZ)
                    ps.setLong(21, e.encounteredMs)
                    ps.addBatch()
                }
                ps.executeBatch()
                conn.commit()
            }
        } catch (ex: SQLException) {
            runCatching { conn.rollback() }
            throw RuntimeException("Failed to flush encounters", ex)
        }

        writeCache.clear()
    }

    fun close() {
        flush()
        runCatching { conn.close() }
    }

    val sizeOnDiskBytes: Long
        get() = try {
            val dir = dbPath.parent
            val base = dbPath.fileName.toString().replace(Regex("\\.db$"), "")
            Files.list(dir).use { stream ->
                stream
                    .filter { it.fileName.toString().startsWith(base) }
                    .mapToLong { runCatching { Files.size(it) }.getOrDefault(0L) }
                    .sum()
            }
        } catch (_: Exception) {
            -1L
        }

    val savedEncounters: Int
        get() = conn.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM encounters").use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }

    val writeCachedCount: Int
        get() = writeCache.size

    val shinyCount: Int
        get() = conn.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM encounters WHERE is_shiny = TRUE").use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }

    val legendaryCount: Int
        get() = conn.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM encounters WHERE is_legendary = TRUE").use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }

    val connection: Connection get() = conn

    companion object {
        private const val FLUSH_THRESHOLD = 4
    }
}
