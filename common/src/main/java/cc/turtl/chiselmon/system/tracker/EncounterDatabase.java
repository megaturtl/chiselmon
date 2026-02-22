package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.PokemonEncounter;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EncounterDatabase {
    private static final int FLUSH_THRESHOLD = 100;

    private final Connection conn;
    private final Map<UUID, PokemonEncounter> writeCache = new LinkedHashMap<>();

    public EncounterDatabase(Connection conn) {
        this.conn = conn;
        try {
            conn.setAutoCommit(false);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init encounter DB schema", e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("""
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
        """);

            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_species    ON encounters(species)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_timestamp  ON encounters(encountered_ms)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_shiny      ON encounters(is_shiny)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_legendary  ON encounters(is_legendary)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_dimension  ON encounters(dimension)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_biome      ON encounters(biome)");
            conn.commit();
        }
    }

    public void record(PokemonEncounter e) {
        writeCache.put(e.uuid(), e);
        if (writeCache.size() >= FLUSH_THRESHOLD) flush();
    }

    public void flush() {
        if (writeCache.isEmpty()) return;

        String encounterSql = """
        MERGE INTO encounters
            (uuid, species, form, level, gender, scale_modifier, is_shiny, is_legendary,
             dimension, biome, world_time, is_raining, block_name, from_snack,
             pokemon_x, pokemon_y, pokemon_z, player_x, player_y, player_z, encountered_ms)
        KEY(uuid)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement eps = conn.prepareStatement(encounterSql)) {
            for (PokemonEncounter e : writeCache.values()) {
                eps.setObject(1, e.uuid());
                eps.setString(2, e.species().getName());
                eps.setString(3, e.form().getName());
                eps.setInt(4, e.level());
                eps.setString(5, e.gender().name());
                eps.setFloat(6, e.scale());
                eps.setBoolean(7, e.isShiny());
                eps.setBoolean(8, e.isLegendary());
                eps.setString(9, e.dimension());
                eps.setString(10, e.biome());
                eps.setLong(11, e.dayTime());
                eps.setBoolean(12, e.isRaining());
                eps.setString(13, e.blockName());
                eps.setBoolean(14, e.spawnedFromSnack());
                eps.setInt(15, e.pokemonX());
                eps.setInt(16, e.pokemonY());
                eps.setInt(17, e.pokemonZ());
                eps.setInt(18, e.playerX());
                eps.setInt(19, e.playerY());
                eps.setInt(20, e.playerZ());
                eps.setLong(21, e.encounteredMs());
                eps.addBatch();
            }

            eps.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to flush encounters", ex);
        }

        writeCache.clear();
    }

    public void close() {
        flush();
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    public int getTotalEncounters() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM encounters")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getShinyCount() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM encounters WHERE is_shiny = TRUE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getLegendaryCount() throws SQLException {
        try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM encounters WHERE is_legendary = TRUE")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}