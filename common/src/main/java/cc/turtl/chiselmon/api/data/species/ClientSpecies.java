package cc.turtl.chiselmon.api.data.species;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A lightweight, immutable representation of Pokemon species data for client-side use.
 *
 * <p>All collections are automatically made unmodifiable and strings are interned
 * for memory efficiency upon construction.
 */
public record ClientSpecies(
        int pokedexNumber,
        String name,
        int catchRate,
        List<String> eggGroups,
        Set<String> labels,
        List<String> aspects,
        int eggCycles,
        Map<String, Integer> baseStats,
        Map<String, Integer> evYield
) {

    public ClientSpecies {
        name = name != null ? name.toLowerCase().intern() : null;
        eggGroups = eggGroups != null ? List.copyOf(eggGroups) : List.of();
        labels = labels != null ? Set.copyOf(labels) : Set.of();
        aspects = aspects != null ? List.copyOf(aspects) : List.of();
        baseStats = baseStats != null ? Map.copyOf(baseStats) : Map.of();
        evYield = evYield != null ? Map.copyOf(evYield) : Map.of();
    }
}