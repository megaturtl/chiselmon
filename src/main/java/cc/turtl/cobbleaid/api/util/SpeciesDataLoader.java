package cc.turtl.cobbleaid.api.util;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side utility for loading species data directly from Cobblemon JSON files.
 * Used as a fallback when data isn't synced to clients on multiplayer servers.
 * Caches loaded data for performance.
 */
public final class SpeciesDataLoader {
    private static final Gson GSON = new Gson();
    private static final String SPECIES_PATH = "species";
    private static final String JSON_EXTENSION = ".json";
    
    // Cache to avoid repeated file reads
    private static final Map<String, SpeciesCache> CACHE = new ConcurrentHashMap<>();

    private SpeciesDataLoader() {
    }

    /**
     * Clears the cache. Should be called on resource reload.
     */
    public static void invalidateCache() {
        CACHE.clear();
    }

    /**
     * Attempts to get egg groups from the Pokemon object first,
     * falls back to loading from species JSON if unavailable (e.g., on servers)
     */
    public static Set<EggGroup> getEggGroups(Pokemon pokemon) {
        if (pokemon == null) {
            return Collections.emptySet();
        }

        FormData formData = pokemon.getForm() != null ? pokemon.getForm() : pokemon.getSpecies().getStandardForm();
        
        // Try to get egg groups normally first
        Set<EggGroup> eggGroups = formData.getEggGroups();
        
        // If empty or null, try loading from JSON cache
        if (eggGroups == null || eggGroups.isEmpty()) {
            SpeciesCache cache = getSpeciesCache(pokemon.getSpecies());
            if (cache != null) {
                FormInfo formInfo = cache.getFormInfo(formData.getName());
                if (formInfo != null && formInfo.eggGroups != null) {
                    eggGroups = formInfo.eggGroups;
                }
            }
        }
        
        return eggGroups != null ? eggGroups : Collections.emptySet();
    }

    /**
     * Attempts to get EV yield from the Pokemon object first,
     * falls back to loading from species JSON if unavailable (e.g., on servers)
     */
    public static Map<Stat, Integer> getEvYield(Pokemon pokemon) {
        if (pokemon == null) {
            return Collections.emptyMap();
        }

        FormData formData = pokemon.getForm() != null ? pokemon.getForm() : pokemon.getSpecies().getStandardForm();
        
        // Try to get EV yield normally first
        Map<Stat, Integer> evYield = formData.getEvYield();
        
        // If empty or null, try loading from JSON cache
        if (evYield == null || evYield.isEmpty()) {
            SpeciesCache cache = getSpeciesCache(pokemon.getSpecies());
            if (cache != null) {
                FormInfo formInfo = cache.getFormInfo(formData.getName());
                if (formInfo != null && formInfo.evYield != null) {
                    evYield = formInfo.evYield;
                }
            }
        }
        
        return evYield != null ? evYield : Collections.emptyMap();
    }

    /**
     * Attempts to get catch rate from the Pokemon object first,
     * falls back to loading from species JSON if unavailable (e.g., on servers)
     */
    public static int getCatchRate(Pokemon pokemon) {
        if (pokemon == null) {
            return 0;
        }

        Species species = pokemon.getSpecies();
        
        // Try to get catch rate normally first
        int catchRate = species.getCatchRate();
        
        // If zero or unavailable, try loading from JSON cache
        if (catchRate == 0) {
            SpeciesCache cache = getSpeciesCache(species);
            if (cache != null) {
                // Catch rate is at species level, not form level
                // Check base form data first
                FormInfo baseInfo = cache.forms.get("");
                if (baseInfo != null && baseInfo.catchRate > 0) {
                    catchRate = baseInfo.catchRate;
                }
            }
        }
        
        return catchRate;
    }

    /**
     * Gets cached species data or loads it from JSON if not cached
     */
    private static SpeciesCache getSpeciesCache(Species species) {
        String key = species.getName().toLowerCase();
        
        return CACHE.computeIfAbsent(key, k -> loadSpeciesData(species));
    }

    /**
     * Loads species data from JSON file
     */
    private static SpeciesCache loadSpeciesData(Species species) {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            
            // Construct the resource location for the species file
            // Format: cobblemon:species/{species_name}.json
            String speciesName = species.getName().toLowerCase();
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath("cobblemon", SPECIES_PATH + "/" + speciesName + JSON_EXTENSION);
            
            Optional<Resource> resource = resourceManager.getResource(location);
            if (resource.isEmpty()) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().open()))) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return parseSpeciesData(json);
            }
        } catch (Exception e) {
            // Silently fail - this is expected for species without JSON files or on servers
            return null;
        }
    }

    /**
     * Parses species JSON into cached data structure.
     * Handles both root-level data and forms array.
     */
    private static SpeciesCache parseSpeciesData(JsonObject json) {
        SpeciesCache cache = new SpeciesCache();
        
        // Parse root level data (this is the standard/base form)
        FormInfo baseForm = parseFormData(json);
        if (baseForm != null) {
            // Store base form data - it may not have a "name" field at root
            // Use empty string or the species name as key for base form
            cache.forms.put("", baseForm);
        }
        
        // Parse forms array if it exists (for regional variants, etc.)
        if (json.has("forms") && json.get("forms").isJsonArray()) {
            JsonArray formsArray = json.getAsJsonArray("forms");
            for (JsonElement formElement : formsArray) {
                if (!formElement.isJsonObject()) {
                    continue;
                }
                
                JsonObject formJson = formElement.getAsJsonObject();
                
                // Each form must have a name
                if (!formJson.has("name")) {
                    continue;
                }
                
                String formName = formJson.get("name").getAsString();
                FormInfo formInfo = parseFormData(formJson);
                
                if (formInfo != null) {
                    cache.forms.put(formName, formInfo);
                }
            }
        }
        
        return cache;
    }

    /**
     * Parses form data from a JSON object (either root or from forms array)
     */
    private static FormInfo parseFormData(JsonObject json) {
        FormInfo formInfo = new FormInfo();
        boolean hasData = false;
        
        // Parse egg groups
        if (json.has("eggGroups") && json.get("eggGroups").isJsonArray()) {
            formInfo.eggGroups = new HashSet<>();
            JsonArray eggGroupsArray = json.getAsJsonArray("eggGroups");
            for (JsonElement eggGroupElement : eggGroupsArray) {
                String groupName = eggGroupElement.getAsString()
                        .toUpperCase()
                        .replace("-", "_");
                try {
                    formInfo.eggGroups.add(EggGroup.valueOf(groupName));
                    hasData = true;
                } catch (IllegalArgumentException e) {
                    // Skip invalid egg groups
                }
            }
        }

        // Parse EV yield
        if (json.has("evYield") && json.get("evYield").isJsonObject()) {
            formInfo.evYield = new HashMap<>();
            JsonObject evYieldJson = json.getAsJsonObject("evYield");
            
            for (String statName : evYieldJson.keySet()) {
                try {
                    Stat stat = Stats.Companion.getStat(statName);
                    if (stat != null) {
                        formInfo.evYield.put(stat, evYieldJson.get(statName).getAsInt());
                        hasData = true;
                    }
                } catch (Exception e) {
                    // Skip invalid stats
                }
            }
        }

        // Parse catch rate (usually at root level, not per-form)
        if (json.has("catchRate")) {
            try {
                formInfo.catchRate = json.get("catchRate").getAsInt();
                hasData = true;
            } catch (Exception e) {
                // Skip invalid catch rate
            }
        }
        
        return hasData ? formInfo : null;
    }

    /**
     * Cache for all forms of a species
     */
    private static class SpeciesCache {
        // Map of form name to form data
        // Empty string "" is used for the base/standard form
        Map<String, FormInfo> forms = new HashMap<>();

        FormInfo getFormInfo(String formName) {
            // Try exact match first
            FormInfo info = forms.get(formName);
            if (info != null) {
                return info;
            }
            
            // Fall back to base form if specific form not found
            return forms.get("");
        }
    }

    /**
     * Cached data for a single form
     */
    private static class FormInfo {
        Set<EggGroup> eggGroups;
        Map<Stat, Integer> evYield;
        int catchRate;
    }
}