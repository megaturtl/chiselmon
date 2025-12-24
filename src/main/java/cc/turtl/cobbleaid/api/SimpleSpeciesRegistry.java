package cc.turtl.cobbleaid.api;

import com.google.gson.Gson;
import cc.turtl.cobbleaid.CobbleAid;
import net.fabricmc.loader.api.FabricLoader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SimpleSpeciesRegistry {
    private static final Map<String, SimpleSpecies> FULL_DATA = new ConcurrentHashMap<>();
    private static final Gson CLEAN_GSON = new Gson();
    private static boolean loaded = false;
    private static boolean isLoading = false;

    public static void loadAsync() {
        if (loaded || isLoading)
            return;
        isLoading = true;

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
                                CobbleAid.getLogger()
                                        .error("Skip " + path.getFileName() + " due to error: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        CobbleAid.getLogger().error("Failed to walk Cobblemon species directory", e);
                    }
                }
            });

            loaded = true;
            isLoading = false;
            long duration = System.currentTimeMillis() - startTime;
            CobbleAid.getLogger().info("Indexed {} species in {}ms (Async POJO mode).", FULL_DATA.size(), duration);
        });
    }

    public static SimpleSpecies getByName(String name) {
        return name == null ? null : FULL_DATA.get(name.toLowerCase());
    }

    public static boolean isLoaded() {
        return loaded;
    }
}