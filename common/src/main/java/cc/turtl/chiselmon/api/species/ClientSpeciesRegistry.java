package cc.turtl.chiselmon.api.species;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.ParseUtils;
import cc.turtl.turtlshell.api.client.ClientEvents;
import cc.turtl.turtlshell.api.core.Platform;
import com.google.gson.Gson;
import kotlin.Unit;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
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
        ClientEvents.INSTANCE.getTICK_POST().subscribe(e -> {
            if (!loaded && !loading) {
                loadAsync();
            }
            return Unit.INSTANCE;
        });

        ClientEvents.INSTANCE.getLEVEL_DISCONNECTED().subscribe(e -> {
            speciesMap = Map.of();
            loaded = false;
            return Unit.INSTANCE;
        });
    }

    private static void loadAsync() {
        loading = true;
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            var tempMap = new ConcurrentHashMap<String, ClientSpecies>(1024);

            Path root = Platform.INSTANCE.findPath("cobblemon", "data/cobblemon/species");
            if (root == null) {
                ChiselmonConstants.LOGGER.error("Cobblemon species path not found!");
                loading = false;
                return;
            }

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

            loading = false;
        });
    }

    private static void parse(Path path, Map<String, ClientSpecies> map) {
        try (Reader reader = Files.newBufferedReader(path)) {
            ClientSpecies species = GSON.fromJson(reader, ClientSpecies.class);
            if (species != null) {
                String fileName = path.getFileName().toString().replace(".json", "");
                String cleanKey = ParseUtils.normalizeSpeciesName(fileName);

                map.put(cleanKey, species);
            }
        } catch (Exception ignored) {
        }
    }

    public static ClientSpecies get(String name) {
        // Apply normalization here so lookups always match the registry keys
        return name == null ? null : speciesMap.get(ParseUtils.normalizeSpeciesName(name));
    }
}