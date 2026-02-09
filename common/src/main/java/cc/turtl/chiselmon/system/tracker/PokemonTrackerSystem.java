package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.api.event.PokemonLoadedEvent;
import cc.turtl.chiselmon.api.event.PokemonUnloadedEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PokemonTrackerSystem {
    private final Map<UUID, PokemonEntity> currentlyLoaded = new HashMap<>();
    private final Map<UUID, PokemonEncounter> seenThisSession = new HashMap<>();

    public PokemonTrackerSystem() {
        registerListeners();
    }

    private void registerListeners() {
        ChiselmonEvents.POKEMON_LOADED.subscribe(Priority.HIGH, this::onLoad);
        ChiselmonEvents.POKEMON_UNLOADED.subscribe(Priority.HIGH, this::onUnload);
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(Priority.HIGH, e -> this.clear());
    }

    private void onLoad(PokemonLoadedEvent event) {
        if (!event.isWild()) return;

        PokemonEntity pe = event.entity();
        currentlyLoaded.put(pe.getUUID(), pe);
        seenThisSession.putIfAbsent(pe.getUUID(), PokemonEncounter.from(pe));
    }

    private void onUnload(PokemonUnloadedEvent event) {
        currentlyLoaded.remove(event.entity().getUUID());
    }

    private void clear() {
        currentlyLoaded.clear();
        seenThisSession.clear();
    }

    public Map<UUID, PokemonEntity> getCurrentlyLoaded() {
        return Collections.unmodifiableMap(currentlyLoaded);
    }

    public Map<UUID, PokemonEncounter> getSeen() {
        return Collections.unmodifiableMap(seenThisSession);
    }
}
