package cc.turtl.chiselmon.feature.spawnalert;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.mixin.accessor.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class AlertManager {
    private final Map<UUID, TrackedPokemon> trackedPokemon = new LinkedHashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>();
    private final SpawnAlertConfig config;
    private int soundDelayTicks = 20;

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void addTarget(PokemonEntity entity, AlertPriority priority) {
        UUID uuid = entity.getUUID();
        TrackedPokemon tracked = new TrackedPokemon(entity);
        tracked.priority = priority; // Store the reason

        if (mutedUuids.contains(uuid)) {
            tracked.muted = true;
        } else {
            AlertMessage.sendChatAlert(entity);
            ((EntityAccessor) entity).invokeSetSharedFlag(6, true);
        }

        trackedPokemon.put(uuid, tracked);
    }

    public TrackedPokemon getTarget(UUID uuid) {
        return trackedPokemon.get(uuid);
    }

    public Set<UUID> getTrackedUuids() {
        return trackedPokemon.keySet();
    }

    public void muteTarget(UUID uuid) {
        mutedUuids.add(uuid);
        TrackedPokemon target = trackedPokemon.get(uuid);
        if (target != null) {
            target.muted = true;
        }
    }

    public void muteTargetByActorId(UUID actorId) {

        Map.Entry<UUID, TrackedPokemon> targetEntry = trackedPokemon.entrySet().stream()
                .filter(entry -> entry.getValue().entity.getPokemon().getUuid().equals(actorId))
                .findFirst()
                .orElse(null);

        if (targetEntry != null) {
            mutedUuids.add(targetEntry.getKey());
            targetEntry.getValue().muted = true;
        }
    }

    public boolean isTargetMuted(UUID uuid) {
        return mutedUuids.contains(uuid);
    }

    public void unmuteAllTargets() {
        mutedUuids.clear();
        trackedPokemon.values().forEach(t -> t.muted = false);
    }

    public void muteAllTargets() {
        trackedPokemon.forEach((uuid, pokemon) -> {
            mutedUuids.add(uuid);
            pokemon.muted = true;
        });
    }

    public boolean hasActiveTarget() {
        return trackedPokemon.values().stream().anyMatch(t -> !t.muted);
    }

    public void clearTargets() {
        mutedUuids.clear();
        trackedPokemon.clear();
    }

    public void removeTarget(UUID uuid) {
        trackedPokemon.remove(uuid);
    }

    public void tick() {
        // Find the highest priority among all tracked pokemon that are NOT muted
        AlertPriority highestActive = trackedPokemon.values().stream()
                .filter(t -> !t.muted)
                .map(t -> t.priority)
                .max((p1, p2) -> Integer.compare(p1.weight, p2.weight))
                .orElse(AlertPriority.NONE);

        if (highestActive == AlertPriority.NONE || config.soundVolume <= 0) {
            return;
        }

        if (soundDelayTicks > 0) {
            soundDelayTicks--;
            return;
        }

        playSound(highestActive);
        this.soundDelayTicks = config.soundDelay;
    }

    private void playSound(AlertPriority priority) {
        AlertSoundProfile profile = priority.soundProfile;
        if (profile == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        float finalVolume = (config.soundVolume / 100f) * profile.volumeMultiplier();

        SimpleSoundInstance alert = SimpleSoundInstance.forUI(
                profile.sound(),
                profile.pitch(),
                finalVolume);

        mc.getSoundManager().play(alert);
    }
}