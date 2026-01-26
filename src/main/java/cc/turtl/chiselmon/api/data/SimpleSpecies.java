package cc.turtl.chiselmon.api.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSpecies {
    public String name = "";
    public int catchRate = 0;
    public int baseExperience = 0;

    @SerializedName("nationalPokedexNumber")
    public int pokedexNumber = -1;

    public List<String> eggGroups = new ArrayList<>();
    public List<String> labels = new ArrayList<>();
    public List<String> aspects = new ArrayList<>();
    public int eggCycles = 50;
    public Map<String, Integer> evYield = new HashMap<>();

    // Transient tells GSON to ignore this field when reading the JSON file
    private transient List<String> formattedEggGroups = null;

    // Using string interning to save ram. Multiple species will share the same
    // string instances for things like "hp", "Fairy", etc. that are repeated a lot
    public void optimize() {

        if (name != null)
            name = name.intern();

        eggGroups = optimizeList(eggGroups);
        labels = optimizeList(labels);
        aspects = optimizeList(aspects);

        // Pre-calculate egg group names once so there's no string logic in the UI
        if (!eggGroups.isEmpty()) {
            formattedEggGroups = eggGroups.stream()
                    .map(SimpleSpecies::formatEggGroupName)
                    .toList();
        } else {
            formattedEggGroups = List.of();
        }

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

    public List<String> getFormattedEggGroups() {
        return formattedEggGroups;
    }

    public static String formatEggGroupName(String internalName) {
        String[] parts = internalName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}