package cc.turtl.cobbleaid.feature.spawnalert;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.cobbleaid.mixin.accessor.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class AlertManager {
    private final Map<UUID, TrackedPokemon> trackedPokemon = new LinkedHashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>();
    private final SpawnAlertConfig config;
    private int soundDelayTicks = 20;

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void addTarget(PokemonEntity entity) {
        UUID uuid = entity.getUUID();
        TrackedPokemon tracked = new TrackedPokemon(entity);

        if (mutedUuids.contains(uuid)) {
            tracked.muted = true;
        } else {
            AlertMessage.sendChatAlert(entity);
            
            // sets the glowing flag client side
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
        if (!hasActiveTarget() || config.soundVolume <= 0) {
            return;
        }

        if (soundDelayTicks > 0) {
            soundDelayTicks--;
            return;
        }

        playSound();

        this.soundDelayTicks = config.soundDelay;
    }

    private void playSound() {
        Minecraft mc = Minecraft.getInstance();

        SimpleSoundInstance alert = SimpleSoundInstance.forUI(
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                1.0f,
                config.soundVolume / 100f);

        mc.getSoundManager().play(alert);
    }
}