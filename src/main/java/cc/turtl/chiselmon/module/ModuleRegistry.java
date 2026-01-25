package cc.turtl.chiselmon.module;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import cc.turtl.chiselmon.Chiselmon;

public final class ModuleRegistry {
    private final Map<String, ChiselmonModule> modules = new LinkedHashMap<>();
    private final Logger logger;

    public ModuleRegistry() {
        this(Chiselmon.getLogger());
    }

    public ModuleRegistry(Logger logger) {
        this.logger = logger;
    }

    public void register(ChiselmonModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }
        String id = module.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Module ID cannot be null or empty");
        }
        if (modules.containsKey(id)) {
            throw new IllegalArgumentException("Module already registered: " + id);
        }
        modules.put(id, module);
        logger.debug("Registered module [{}]", id);
    }

    public void initializeModules() {
        for (ChiselmonModule module : modules.values()) {
            module.initialize();
        }
        logger.debug("Initialized {} modules.", modules.size());
    }

    public boolean isLoaded(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return modules.containsKey(id);
    }

    public Collection<ChiselmonModule> modules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public <T extends ChiselmonModule> T getModule(Class<T> type) {
        for (ChiselmonModule module : modules.values()) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }
}
