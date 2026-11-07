package cc.turtl.chiselmon.client.system.dashboard.api

import cc.turtl.chiselmon.client.system.dashboard.api.endpoints.HeatmapHandler
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import java.sql.Connection
import java.time.Duration
import java.util.UUID
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DashboardQueryTest {
    @TempDir lateinit var root: Path

    private val dimension = "minecraft:overworld' OR 1=1 --"

    @Test
    fun `heatmap treats SQL in a dimension as data and preserves time and coordinate filters`() {
        JdbcDataSource()
            .apply { setURL("jdbc:h2:mem:") }
            .connection
            .use { connection ->
                val db = database(connection)
                val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
                server.createContext("/api/heatmap", HeatmapHandler(db))
                server.start()
                try {
                    HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build().use {
                        client ->
                        val uri =
                            URI.create(
                                "http://127.0.0.1:${server.address.port}/api/heatmap" +
                                    "?cx=0&cz=0&radius=16&from=200&to=200&dimension=" +
                                    URLEncoder.encode(dimension, UTF_8)
                            )
                        val request =
                            HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).build()
                        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                        val body = JsonParser.parseString(response.body()).asJsonObject
                        assertEquals(200, response.statusCode())
                        assertEquals(JsonParser.parseString("[8,9]"), body.get("pokemon"))
                        assertEquals(JsonParser.parseString("[10,11]"), body.get("player"))
                    }
                } finally {
                    server.stop(0)
                }
            }
    }

    @Test
    fun `all fetch modes bind values in order and count ignores the result limit`() {
        JdbcDataSource()
            .apply { setURL("jdbc:h2:mem:") }
            .connection
            .use { connection ->
                val db = database(connection)
                val query =
                    QueryBuilder(db.connection, "encounters")
                        .select("pokemon_x, pokemon_z")
                        .where("dimension = ?", dimension)
                        .timeRange(TimeRange(200, 200))
                        .orderBy("pokemon_x")
                        .limit(1)

                assertEquals(listOf(8), query.fetchList { it.getInt("pokemon_x") })
                assertEquals(8, query.fetchOne { it.getInt("pokemon_x") })
                assertEquals(2L, query.fetchCount())
                assertArrayEquals(
                    intArrayOf(8, 9),
                    query.fetchInterleavedPairs("pokemon_x", "pokemon_z"),
                )
            }
    }

    private fun database(connection: Connection): EncounterDatabase {
        val db = EncounterDatabase(connection, root.resolve("encounters"))
        connection
            .prepareStatement(
                """
                INSERT INTO encounters (uuid, species, level, gender, scale_modifier,
                    is_shiny, is_legendary, dimension, biome, world_time, is_raining,
                    block_name, from_snack, pokemon_x, pokemon_y, pokemon_z,
                    player_x, player_y, player_z, encountered_ms)
                VALUES (?, 'Pikachu', 25, 'MALE', 1, FALSE, FALSE, ?, 'minecraft:plains',
                    0, FALSE, 'minecraft:grass_block', FALSE, ?, 64, ?, ?, 64, ?, ?)
                """
                    .trimIndent()
            )
            .use { statement ->
                val rows =
                    listOf(
                        Triple("minecraft:overworld", 2, 200L),
                        Triple("minecraft:the_nether", 4, 200L),
                        Triple(dimension, 8, 200L),
                        Triple(dimension, 60, 200L),
                        Triple(dimension, 12, 300L),
                    )
                for ((rowDimension, x, time) in rows) {
                    statement.setObject(1, UUID.randomUUID())
                    statement.setString(2, rowDimension)
                    statement.setInt(3, x)
                    statement.setInt(4, x + 1)
                    statement.setInt(5, x + 2)
                    statement.setInt(6, x + 3)
                    statement.setLong(7, time)
                    statement.executeUpdate()
                }
                connection.commit()
            }
        return db
    }
}
