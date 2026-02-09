package cc.turtl.chiselmon.api.data.species;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.platform.PlatformHelper;
import com.google.gson.Gson;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class ClientSpeciesRegistry {
    private static final Gson GSON = new Gson();

    private Map<String, ClientSpecies> speciesMap = Map.of();
    private boolean loaded = false;
    private boolean loading = false;

    public ClientSpeciesRegistry() {
        registerEventListeners();
    }

    private void registerEventListeners() {
        ChiselmonEvents.CLIENT_POST_TICK.subscribe(e -> {
            // Only attempt load if in-world and not already working
            if (e.minecraft().level != null && !loaded && !loading) {
                this.loadAsync();
            }
        });

        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(e -> {
            this.speciesMap = Map.of();
            this.loaded = false;
        });
    }

    /**
     * Discovers and parses species files async using the provided path finder.
     */
    private void loadAsync() {
        this.loading = true;
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            var tempMap = new ConcurrentHashMap<String, ClientSpecies>(1024);
            var pathFinder = PlatformHelper.getPathFinder();

            pathFinder.getPath("cobblemon", "data/cobblemon/species").ifPresentOrElse(root -> {
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.parallel()
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> parse(p, tempMap));

                    this.speciesMap = Map.copyOf(tempMap);
                    this.loaded = true;
                    ChiselmonConstants.LOGGER.info("Indexed {} species in {}ms.", speciesMap.size(), System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    ChiselmonConstants.LOGGER.error("Failed indexing species: ", e);
                }
            }, () -> ChiselmonConstants.LOGGER.error("Cobblemon species path not found!"));

            this.loading = false;
        });
    }

    private void parse(Path path, Map<String, ClientSpecies> map) {
        try (Reader reader = Files.newBufferedReader(path)) {
            ClientSpecies species = GSON.fromJson(reader, ClientSpecies.class);
            if (species != null && species.name() != null) {
                map.put(species.name(), species);
            }
        } catch (Exception ignored) {}
    }

    public ClientSpecies get(String name) {
        return name == null ? null : speciesMap.get(name.toLowerCase());
    }

    public boolean isLoaded() {
        return loaded;
    }
}