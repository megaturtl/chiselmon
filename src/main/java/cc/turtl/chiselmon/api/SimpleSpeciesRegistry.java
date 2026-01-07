package cc.turtl.chiselmon.api;

import com.google.gson.Gson;

import cc.turtl.chiselmon.Chiselmon;
import net.fabricmc.loader.api.FabricLoader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SimpleSpeciesRegistry {
    private static final Map<String, SimpleSpecies> FULL_DATA = new ConcurrentHashMap<>();
    private static final Gson CLEAN_GSON = new Gson();
    private static final AtomicBoolean isLoading = new AtomicBoolean(false);
    private static volatile boolean loaded = false;

    public static void loadAsync() {
        // Atomic check to prevent double-loading
        if (!isLoading.compareAndSet(false, true))
            return;
        if (loaded) {
            isLoading.set(false);
            return;
        }

        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            FabricLoader.getInstance().getModContainer("cobblemon").ifPresent(mod -> {
                Path speciesRoot = mod.findPath("data/cobblemon/species").orElse(null);
                if (speciesRoot != null) {
                    try (Stream<Path> walk = Files.walk(speciesRoot)) {
                        walk.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                            try (Reader reader = new InputStreamReader(Files.newInputStream(path))) {

                                SimpleSpecies species = CLEAN_GSON.fromJson(reader, SimpleSpecies.class);

                                if (species != null && species.name != null) {
                                    // Use name as the key for easy lookup
                                    FULL_DATA.put(species.name.toLowerCase(), species);
                                }
                            } catch (Exception e) {
                                // This will no longer crash on abilities!
                                Chiselmon.getLogger()
                                        .error("Skip " + path.getFileName() + " due to error: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        Chiselmon.getLogger().error("Failed to walk Cobblemon species directory", e);
                    }
                }
            });

            loaded = true;
            isLoading.set(false);
            long duration = System.currentTimeMillis() - startTime;
            Chiselmon.getLogger().info("Indexed {} species in {}ms (Async POJO mode).", FULL_DATA.size(), duration);
        });
    }

    public static SimpleSpecies getByName(String name) {
        return name == null ? null : FULL_DATA.get(name.toLowerCase());
    }

    public static boolean isLoaded() {
        return loaded;
    }
}