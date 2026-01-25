package cc.turtl.chiselmon.module;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleRegistry {
    private final Map<String, ChiselmonModule> modules = new LinkedHashMap<>();

    public void register(ChiselmonModule module) {
        if (module == null) {
            return;
        }
        modules.put(module.id(), module);
    }

    public void initializeModules() {
        for (ChiselmonModule module : modules.values()) {
            module.initialize();
        }
    }

    public boolean isLoaded(String id) {
        return modules.containsKey(id);
    }

    public Collection<ChiselmonModule> modules() {
        return Collections.unmodifiableCollection(modules.values());
    }
}
