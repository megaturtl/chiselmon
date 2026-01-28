package cc.turtl.chiselmon.api.data.species;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.services.IPathFinder;
import com.google.gson.Gson;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public final class ClientSpeciesRegistry {
    private static final Gson GSON = new Gson();
    private static Map<String, ClientSpecies> FULL_DATA = Map.of();

    private static final AtomicBoolean LOADING = new AtomicBoolean(false);
    private static volatile boolean loaded = false;

    private ClientSpeciesRegistry() {}

    /**
     * Discovers and parses species files async using the provided path finder.
     */
    public static void loadAsync(IPathFinder pathFinder) {
        if (loaded || !LOADING.compareAndSet(false, true)) return;

        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            var loadingMap = new ConcurrentHashMap<String, ClientSpecies>(1024);

            pathFinder.getPath("cobblemon", "data/cobblemon/species").ifPresentOrElse(root -> {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.parallel()
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> parse(p, loadingMap));

                    FULL_DATA = Map.copyOf(loadingMap);
                    loaded = true;
                } catch (Exception e) {
                    ChiselmonConstants.LOGGER.error("Failed indexing species: ", e);
                }
            }, () -> ChiselmonConstants.LOGGER.error("Cobblemon species path not found!"));

            LOADING.set(false);
            ChiselmonConstants.LOGGER.info("Indexed {} species in {}ms.", FULL_DATA.size(), System.currentTimeMillis() - startTime);
        });
    }

    private static void parse(Path path, Map<String, ClientSpecies> map) {
        try (Reader reader = Files.newBufferedReader(path)) {
            ClientSpecies species = GSON.fromJson(reader, ClientSpecies.class);
            if (species != null && species.name() != null) {
                // record optimization: intern the name and store the optimized record
                map.put(species.name().toLowerCase().intern(), species.optimize());
            }
        } catch (Exception ignored) {
        }
    }

    public static ClientSpecies get(String name) {
        return name == null ? null : FULL_DATA.get(name.toLowerCase());
    }

    public static boolean isLoaded() { return loaded; }
}