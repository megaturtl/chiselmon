package cc.turtl.chiselmon.module;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.world.entity.Entity;

public abstract class AbstractEntityMonitor implements ChiselmonModule {
    public static final String ID = "entity-monitor";
    private final Map<UUID, PokemonEntity> loadedEntities = new HashMap<>();

    @Override
    public String id() {
        return ID;
    }

    protected void handleEntityLoad(Entity entity) {
        if (entity instanceof PokemonEntity pe) {
            loadedEntities.put(pe.getUUID(), pe);
        }
    }

    protected void handleEntityUnload(Entity entity) {
        if (entity instanceof PokemonEntity pe) {
            loadedEntities.remove(pe.getUUID());
        }
    }

    protected void handleClear() {
        loadedEntities.clear();
    }

    public Map<UUID, PokemonEntity> getLoadedEntities() {
        return loadedEntities;
    }
}