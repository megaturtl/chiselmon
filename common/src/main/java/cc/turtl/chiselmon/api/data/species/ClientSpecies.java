package cc.turtl.chiselmon.api.data.species;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A lightweight, immutable representation of Pokemon species data for client-side use.
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

        public boolean isLegendary() {
                return labels.contains("legendary");
        }

        /**
         * Optimization method to ensure lists/sets are unmodifiable
         * and strings are interned to save memory.
         */
        public ClientSpecies optimize() {
                return new ClientSpecies(
                        pokedexNumber,
                        name.toLowerCase().intern(),
                        catchRate,
                        List.copyOf(eggGroups),
                        Set.copyOf(labels),
                        List.copyOf(aspects),
                        eggCycles,
                        Map.copyOf(baseStats),
                        Map.copyOf(evYield)
                );
        }
}