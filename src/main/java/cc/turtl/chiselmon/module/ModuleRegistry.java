package cc.turtl.chiselmon.module;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleRegistry {
    private final Map<String, ChiselmonModule> modules = new LinkedHashMap<>();

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
    }

    public void initializeModules() {
        for (ChiselmonModule module : modules.values()) {
            module.initialize();
        }
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
}
