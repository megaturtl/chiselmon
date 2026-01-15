package cc.turtl.chiselmon.feature.spawnalert.experimental;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;

public final class SpawnAlertState {
    private final Map<UUID, AlertPriority> targets = new HashMap<>();
    private boolean mutedAll = false;
    private long lastSoundTick = -1;

    public Map<UUID, AlertPriority> targetsView() {
        return Collections.unmodifiableMap(targets);
    }

    public void putTarget(UUID id, AlertPriority priority) {
        targets.put(id, priority);
    }

    public void removeTarget(UUID id) {
        targets.remove(id);
    }

    public void clearTargets() {
        targets.clear();
    }

    public boolean isMutedAll() {
        return mutedAll;
    }

    public void setMutedAll(boolean mutedAll) {
        this.mutedAll = mutedAll;
    }

    public long getLastSoundTick() {
        return lastSoundTick;
    }

    public void setLastSoundTick(long lastSoundTick) {
        this.lastSoundTick = lastSoundTick;
    }
}
