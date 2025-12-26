package cc.turtl.cobbleaid.feature.hud.spawntracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import cc.turtl.cobbleaid.feature.spawntracker.SpawnEntry;
import cc.turtl.cobbleaid.feature.spawntracker.SpawnResponseParser;

public class SpawnResponseParserTest {

    @Test
    void parsesCombinedResponseLine() {
        List<String> lines = List.of(
                "Nearby spawns:",
                "Dreepy: 0.07%, Bulbasaur: 5%, Rattata: 11%");

        List<SpawnEntry> entries = SpawnResponseParser.parse(lines);

        assertEquals(3, entries.size());
        assertEquals("Dreepy", entries.get(0).name());
        assertEquals(0.07F, entries.get(0).percentage(), 0.0001F);
        assertEquals("Bulbasaur", entries.get(1).name());
        assertEquals(5F, entries.get(1).percentage(), 0.0001F);
    }

    @Test
    void parsesSeparatedLines() {
        List<String> lines = List.of(
                "Zorua: 0.5%",
                "Murkrow: 1.25%",
                "Scyther: 12.75%");

        List<SpawnEntry> entries = SpawnResponseParser.parse(lines);

        assertEquals(3, entries.size());
        assertEquals("Murkrow", entries.get(1).name());
        assertEquals(1.25F, entries.get(1).percentage(), 0.0001F);
    }

    @Test
    void ignoresNonSpawnLines() {
        List<String> lines = List.of(
                "command.checkspawns.nothing",
                "No spawnable Pokémon nearby");

        List<SpawnEntry> entries = SpawnResponseParser.parse(lines);

        assertTrue(entries.isEmpty());
    }
}
