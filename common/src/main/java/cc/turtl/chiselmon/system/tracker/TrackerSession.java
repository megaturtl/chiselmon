package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.event.PokemonLoadedEvent;
import cc.turtl.chiselmon.api.event.PokemonUnloadedEvent;
import cc.turtl.chiselmon.util.render.PokemonEntityUtils;
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

    public void onPokemonLoad(PokemonLoadedEvent event) {
        if (!event.isWild()) return;

        PokemonEntity entity = event.entity();
        UUID uuid = entity.getUUID();

        encounters.put(uuid, event.encounterSnapshot());
        currentlyLoaded.put(uuid, entity);
    }

    public void onPokemonUnload(PokemonUnloadedEvent event) {
        PokemonEntity entity = event.entity();

        currentlyLoaded.remove(entity.getUUID());
    }

    public void tick() {
        cleanUnloaded();

        // reset these always, before despawn glow or alerts, so they always work from a blank slate.
        for (PokemonEntity entity : currentlyLoaded.values()) {
            PokemonEntityUtils.removeGlow(entity);
            PokemonEntityUtils.resetNickname(entity);
        }
    }

    /**
     * Cleans up any accidental stale references to unloaded pokemon
     */
    private void cleanUnloaded() {
        currentlyLoaded.entrySet().removeIf(e -> e.getValue().isRemoved());
    }

    public Map<UUID, PokemonEntity> getCurrentlyLoaded() {
        return currentlyLoaded;
    }

    public long getMsElapsed() {
        return System.currentTimeMillis() - startTimeMs;
    }

    public int getEncounterCount() {
        return encounters.size();
    }
}