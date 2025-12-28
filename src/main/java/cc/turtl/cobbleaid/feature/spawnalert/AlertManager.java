package cc.turtl.cobbleaid.feature.spawnalert;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class AlertManager {
    private final Map<UUID, TrackedPokemon> trackedPokemon = new LinkedHashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>();
    private final SpawnAlertConfig config;
    private int soundDelayTicks = 0;

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
        }

        trackedPokemon.put(uuid, tracked);
    }

    public void muteTarget(UUID uuid) {
        mutedUuids.add(uuid);
        TrackedPokemon target = trackedPokemon.get(uuid);
        if (target != null) {
            target.muted = true;
        }
    }

    public void unmuteAllTargets() {
        mutedUuids.clear();
        trackedPokemon.values().forEach(t -> t.muted = false);
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