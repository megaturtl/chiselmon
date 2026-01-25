package cc.turtl.chiselmon.api.data;

import com.google.gson.Gson;

import cc.turtl.chiselmon.Chiselmon;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleSpeciesRegistry {
    private static Map<String, SimpleSpecies> FULL_DATA = new HashMap<>();
    private static final Gson CLEAN_GSON = new Gson();

    // AtomicBoolean prevents multiple threads from starting the load process at the same time
    private static final AtomicBoolean isLoading = new AtomicBoolean(false);
    // Volatile so all cores see the "true" value of the boolean immediately
    private static volatile boolean loaded = false;

    public static void loadAsync() {
        if (!isLoading.compareAndSet(false, true))
            return;
        if (loaded) {
            isLoading.set(false);
            return;
        }

        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            // Use a ConcurrentMap only during the multi-threaded loading phase
            ConcurrentHashMap<String, SimpleSpecies> loadingMap = new ConcurrentHashMap<>(1024);

            // Platform-specific implementation should override this method to load species data
            // For now, we'll just mark as loaded to prevent errors
            Chiselmon.getLogger().warn("SimpleSpeciesRegistry loading skipped - platform-specific implementation required");

            // Map.copyOf creates a smaller unmodifiable map (uses less ram than HashMap)
            FULL_DATA = Map.copyOf(loadingMap);
            loaded = true;
            isLoading.set(false);

            long duration = System.currentTimeMillis() - startTime;
            Chiselmon.getLogger().info("Indexed {} species in {}ms.", FULL_DATA.size(), duration);
        });
    }

    public static SimpleSpecies getByName(String name) {
        return name == null ? null : FULL_DATA.get(name.toLowerCase());
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
