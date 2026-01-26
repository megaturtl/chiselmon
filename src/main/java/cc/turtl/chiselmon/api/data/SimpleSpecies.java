package cc.turtl.chiselmon.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSpecies {
    public String name = "";
    public int catchRate = 0;
    public List<String> eggGroups = new ArrayList<>();
    public List<String> labels = new ArrayList<>();
    public List<String> aspects = new ArrayList<>();
    public int eggCycles = 50;
    public Map<String, Integer> evYield = new HashMap<>();

    // Using string interning to save ram. Multiple species will share the same
    // string instances for things like "hp", "Fairy", etc. that are repeated a lot
    public void optimize() {

        if (name != null)
            name = name.intern();

        eggGroups = optimizeList(eggGroups);
        labels = optimizeList(labels);
        aspects = optimizeList(aspects);

        if (evYield == null || evYield.isEmpty()) {
            evYield = Map.of(); // Shares a single empty map instance for all species
        } else {
            // Intern the keys here too, since "hp" and "attack" repeat thousands of times
            Map<String, Integer> internedEVs = new HashMap<>();
            evYield.forEach((k, v) -> internedEVs.put(k.intern(), v));
            evYield = Map.copyOf(internedEVs); // Shrinks the map to its smallest possible size
        }
    }

    private List<String> optimizeList(List<String> list) {
        if (list == null || list.isEmpty())
            return List.of();
        // Intern every string in the list and freeze the list size
        return list.stream()
                .map(String::intern)
                .toList();
    }
}