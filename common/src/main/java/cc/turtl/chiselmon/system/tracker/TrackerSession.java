package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.event.PokemonLoadedEvent;
import cc.turtl.chiselmon.api.event.PokemonUnloadedEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackerSession {
    private final long startTimeMs;
    private final Map<UUID, PokemonEncounter> encounters = new HashMap<>();
    private final Map<UUID, PokemonEntity> currentlyLoaded = new HashMap<>();

    public TrackerSession() {
        startTimeMs = System.currentTimeMillis();
    }

    public void trackPokemon(PokemonLoadedEvent event) {
        if (!event.isWild()) return;

        PokemonEntity entity = event.entity();
        encounters.put(entity.getUUID(), event.encounterSnapshot());
        currentlyLoaded.put(entity.getUUID(), entity);
    }

    public void untrackPokemon(PokemonUnloadedEvent event) {
        currentlyLoaded.remove(event.entity().getUUID());
    }

    /**
     * Cleans up any accidental stale references to unloaded pokemon
     */
    private void cleanUnloaded() {
        currentlyLoaded.entrySet().removeIf(e -> e.getValue().isRemoved());
    }

    public Map<UUID, PokemonEntity> getCurrentlyLoaded() {
        cleanUnloaded();
        return currentlyLoaded;
    }

    public long getMsElapsed() {
        return System.currentTimeMillis() - startTimeMs;
    }

    public int getEncounterCount() {
        return encounters.size();
    }
}