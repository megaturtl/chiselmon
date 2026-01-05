package cc.turtl.chiselmon.api;

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

    @SerializedName("eggGroups")
    public List<String> eggGroups = new ArrayList<>();

    @SerializedName("labels")
    public List<String> labels = new ArrayList<>();

    @SerializedName("aspects")
    public List<String> aspects = new ArrayList<>();

    @SerializedName("eggCycles")
    public int eggCycles = 50;

    // Map keys will be "hp", "attack", "defence", "special_attack", etc.
    public Map<String, Integer> evYield = new HashMap<>();

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