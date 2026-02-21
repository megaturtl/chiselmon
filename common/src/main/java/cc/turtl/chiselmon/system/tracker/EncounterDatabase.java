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
                            uuid               UUID    PRIMARY KEY,
                            species            VARCHAR(64)  NOT NULL,
                            form               VARCHAR(64),
                            level              INT     NOT NULL,
                            scale_modifier     FLOAT   NOT NULL,
                            is_shiny           BOOLEAN NOT NULL,
                            is_legendary       BOOLEAN NOT NULL,
                            is_extreme_size    BOOLEAN NOT NULL,
                            from_snack         BOOLEAN NOT NULL,
                            player_x           INT     NOT NULL,
                            player_y           INT     NOT NULL,
                            player_z           INT     NOT NULL,
                            pokemon_x          INT     NOT NULL,
                            pokemon_y          INT     NOT NULL,
                            pokemon_z          INT     NOT NULL,
                            dimension          VARCHAR(128) NOT NULL,
                            biome              VARCHAR(128) NOT NULL,
                            encountered_at_ms  BIGINT  NOT NULL,
                            encountered_at     TIMESTAMP AS DATEADD('MILLISECOND', encountered_at_ms, TIMESTAMP '1970-01-01 00:00:00')
                        )
                    """);
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_species     ON encounters(species)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_timestamp   ON encounters(encountered_at_ms)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_shiny       ON encounters(is_shiny)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_legendary   ON encounters(is_legendary)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_dimension   ON encounters(dimension)");
            conn.commit();
        }
    }

    public void record(PokemonEncounter e) {
        writeCache.put(e.uuid(), e);
        if (writeCache.size() >= FLUSH_THRESHOLD) flush();
    }

    public void flush() {
        if (writeCache.isEmpty()) return;
        String sql = """
                    MERGE INTO encounters
                        (uuid, species, form, level, scale_modifier, is_shiny, is_legendary,
                         is_extreme_size, from_snack, player_x, player_y, player_z,
                         pokemon_x, pokemon_y, pokemon_z, dimension, biome, encountered_at_ms)
                    KEY(uuid)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PokemonEncounter e : writeCache.values()) {
                ps.setObject(1, e.uuid());
                ps.setString(2, e.species().getName());
                ps.setString(3, e.form().getName());
                ps.setInt(4, e.level());
                ps.setFloat(5, e.scaleModifier());
                ps.setBoolean(6, e.isShiny());
                ps.setBoolean(7, e.isLegendary());
                ps.setBoolean(8, e.isExtremeSize());
                ps.setBoolean(9, e.spawnedFromSnack());
                ps.setInt(10, e.playerX());
                ps.setInt(11, e.playerY());
                ps.setInt(12, e.playerZ());
                ps.setInt(13, e.pokemonX());
                ps.setInt(14, e.pokemonY());
                ps.setInt(15, e.pokemonZ());
                ps.setString(16, e.dimension());
                ps.setString(17, e.biome());
                ps.setLong(18, e.encounteredAtMs());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
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