package cc.turtl.chiselmon.module;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cc.turtl.chiselmon.Chiselmon;

/**
 * This registry handles setting up each module and providing module instances
 * to external classes that might need them.
 */
public final class ModuleRegistry {
    private final Map<String, ChiselmonModule> modules = new LinkedHashMap<>();
    private final Map<Class<? extends ChiselmonModule>, ChiselmonModule> typeCache = new HashMap<>();
    private final Logger logger;

    public ModuleRegistry() {
        this(Chiselmon.getLogger());
    }

    public ModuleRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Registers a module and caches its class type for fast lookup.
     * 
     * @throws IllegalArgumentException if ID is invalid or already registered.
     */
    public void register(@NotNull ChiselmonModule module) {
        String id = module.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Module ID cannot be null or empty");
        }

        if (modules.containsKey(id)) {
            throw new IllegalArgumentException("Module already registered: " + id);
        }

        modules.put(id, module);
        typeCache.put(module.getClass(), module);
        logger.debug("Registered module [{}]", id);
    }

    public void initializeModules() {
        modules.values().forEach(ChiselmonModule::initialize);
        logger.debug("Initialized {} modules.", modules.size());
    }

    public boolean isLoaded(@Nullable String id) {
        return id != null && !id.isBlank() && modules.containsKey(id);
    }

    @NotNull
    public Collection<ChiselmonModule> modules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    /**
     * Get the instance of a module class if registered.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ChiselmonModule> T getModule(@NotNull Class<T> type) {
        return (T) typeCache.get(type);
    }
}