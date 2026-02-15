package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.api.OLDChiselmonConfig;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.system.tracker.PokemonTrackerSystem;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.*;

public class PokemonAlertSystem {
    private final PokemonTrackerSystem tracker;
    private final AlertTicker ticker;
    private final Set<UUID> mutedUuids = new HashSet<>();

    public PokemonAlertSystem(PokemonTrackerSystem tracker) {
        AlertConfig config = AutoConfig.getConfigHolder(OLDChiselmonConfig.class).getConfig().alert;
        this.tracker = tracker;
        this.ticker = new AlertTicker(this, config);
        registerListeners();
    }

    private void registerListeners() {
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(Priority.HIGH, e -> {
            this.unmuteAll();
            ticker.clear();
        });
        ChiselmonEvents.CLIENT_POST_TICK.subscribe(e -> ticker.tick());
    }

    public void mute(UUID uuid) {
        mutedUuids.add(uuid);
    }

    public void muteAll() {
        mutedUuids.addAll(tracker.getCurrentlyLoaded().keySet());
    }

    public void unmuteAll() {
        mutedUuids.clear();
    }

    public boolean isMuted(UUID uuid) {
        return mutedUuids.contains(uuid);
    }

    public Collection<PokemonEntity> getLoadedPokemonEntities() {
        return tracker.getCurrentlyLoaded().values();
    }

    public Set<UUID> getMutedUuids() {
        return this.mutedUuids;
    }
}