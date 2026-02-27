package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.ChiselmonStorage;
import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.event.PokemonLoadedEvent;
import cc.turtl.chiselmon.api.event.PokemonUnloadedEvent;
import cc.turtl.chiselmon.api.storage.StorageScope;
import cc.turtl.chiselmon.system.dashboard.DashboardServer;
import cc.turtl.chiselmon.util.render.PokemonEntityUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.io.IOException;
import java.util.*;

/**
 * Keeps track of currently loaded WILD pokemon, and stores all new encounters in a database.
 */
public class TrackerSession {
    private static final int DASHBOARD_PORT = 7890;

    private final long startTimeMs;
    private final Map<UUID, PokemonEntity> currentlyLoaded = new HashMap<>();
    private final Set<UUID> seenUuids = new HashSet<>();
    private final EncounterDatabase db;
    private DashboardServer dashboardServer;

    public TrackerSession() {
        this.startTimeMs = System.currentTimeMillis();
        this.db = ChiselmonStorage.ENCOUNTERS.get(StorageScope.currentWorld());
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

        // reset these always, before despawn glow or alert, so they always work from a blank slate.
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

    public void startDashboard() throws IOException {
        if (dashboardServer != null) return;
        dashboardServer = new DashboardServer(db, DASHBOARD_PORT);
        dashboardServer.start();
    }

    public void stopDashboard() {
        if (dashboardServer == null) return;
        dashboardServer.stop();
        dashboardServer = null;
    }

    public boolean isDashboardRunning() {
        return dashboardServer != null;
    }

    public int getDashboardPort() {
        return DASHBOARD_PORT;
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