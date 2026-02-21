package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.event.PokemonLoadedEvent;
import cc.turtl.chiselmon.api.event.PokemonUnloadedEvent;
import cc.turtl.chiselmon.data.ChiselmonData;
import cc.turtl.chiselmon.data.Scope;
import cc.turtl.chiselmon.util.render.PokemonEntityUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.*;

public class TrackerSession {
    private final long startTimeMs;
    private final Map<UUID, PokemonEntity> currentlyLoaded = new HashMap<>();
    private final Set<UUID> seenUuids = new HashSet<>();
    private final EncounterDatabase db;

    public TrackerSession() {
        this.startTimeMs = System.currentTimeMillis();
        this.db = ChiselmonData.ENCOUNTERS.get(Scope.currentWorld());
    }

    public void onPokemonLoad(PokemonLoadedEvent event) {
        if (!event.isWild()) return;

        PokemonEncounter encounter = event.encounterSnapshot();
        UUID uuid = event.entity().getUUID();

        currentlyLoaded.put(uuid, event.entity());
        seenUuids.add(uuid);
        db.record(encounter);
    }

    public void onPokemonUnload(PokemonUnloadedEvent event) {
        currentlyLoaded.remove(event.entity().getUUID());
    }

    public void tick() {
        cleanUnloaded();

        // reset these always, before despawn glow or alerts, so they always work from a blank slate.
        for (PokemonEntity entity : currentlyLoaded.values()) {
            PokemonEntityUtils.removeGlow(entity);
            PokemonEntityUtils.resetNickname(entity);
        }
    }

    private void cleanUnloaded() {
        currentlyLoaded.entrySet().removeIf(e -> e.getValue().isRemoved());
    }

    public EncounterDatabase getDb() {
        return db;
    }

    public Map<UUID, PokemonEntity> getCurrentlyLoaded() {
        return currentlyLoaded;
    }

    public long getMsElapsed() {
        return System.currentTimeMillis() - startTimeMs;
    }

    public int getEncounterCount() {
        return seenUuids.size();
    }
}