package cc.turtl.chiselmon.api.species;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.platform.PlatformServices;
import com.google.gson.Gson;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ClientSpeciesRegistry {
    private static final Gson GSON = new Gson();

    private static Map<String, ClientSpecies> speciesMap = Map.of();
    private static boolean loaded = false;
    private static boolean loading = false;

    public static void init() {
        ChiselmonEvents.CLIENT_POST_TICK.subscribe(e -> {
            if (e.mc().level != null && !loaded && !loading) {
                loadAsync();
            }
        });

        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(e -> {
            speciesMap = Map.of();
            loaded = false;
        });
    }

    private static void loadAsync() {
        loading = true;
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            var tempMap = new ConcurrentHashMap<String, ClientSpecies>(1024);
            var pathFinder = PlatformServices.getPathFinder();

            pathFinder.getModPath("cobblemon", "data/cobblemon/species").ifPresentOrElse(root -> {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.parallel()
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> parse(p, tempMap));

                    speciesMap = Map.copyOf(tempMap);
                    loaded = true;
                    ChiselmonConstants.LOGGER.info("Indexed {} species in {}ms.", speciesMap.size(), System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    ChiselmonConstants.LOGGER.error("Failed indexing species: ", e);
                }
            }, () -> ChiselmonConstants.LOGGER.error("Cobblemon species path not found!"));

            loading = false;
        });
    }

    private static void parse(Path path, Map<String, ClientSpecies> map) {
        try (Reader reader = Files.newBufferedReader(path)) {
            ClientSpecies species = GSON.fromJson(reader, ClientSpecies.class);
            if (species != null) {
                // Get "flutter_mane.json" -> "flutter_mane" -> "fluttermane"
                String fileName = path.getFileName().toString().replace(".json", "");
                String cleanKey = cleanString(fileName);

                map.put(cleanKey, species);
            }
        } catch (Exception ignored) {
        }
    }

    public static ClientSpecies get(String name) {
        return name == null ? null : speciesMap.get(cleanString(name));
    }

    // Centralized helper to ensure the Registry and callers always match species names
    public static String cleanString(String input) {
        if (input == null) return "";

        // 1. Normalize first (Decompose é into e + accent)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // 2. Remove the accent marks (\p{M})
        String stripped = normalized.replaceAll("\\p{M}", "");

        // 3. Lowercase and remove remaining non-alphanumerics
        return stripped.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}