package cc.turtl.chiselmon.feature.spawnalert;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.event.AlertMutedEvent;
import cc.turtl.chiselmon.event.EventBus;
import cc.turtl.chiselmon.event.PokemonTrackedEvent;
import cc.turtl.chiselmon.event.PokemonUntrackedEvent;
import cc.turtl.chiselmon.feature.spawnalert.action.AlertAction;
import cc.turtl.chiselmon.feature.spawnalert.action.ChatAlertAction;
import cc.turtl.chiselmon.mixin.accessor.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/**
 * Manages the collection of tracked Pokemon and their alert states.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Maintaining the set of tracked Pokemon entities</li>
 *   <li>Managing mute states for alerts</li>
 *   <li>Executing alert actions when Pokemon are added</li>
 *   <li>Playing alert sounds on tick</li>
 *   <li>Publishing events for other systems to react to</li>
 * </ul>
 * 
 * <p>The AlertManager publishes events via {@link EventBus} to allow
 * other features to react to tracking changes without direct coupling.
 */
public class AlertManager {
    private final Map<UUID, TrackedPokemon> trackedPokemon = new LinkedHashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>();
    private final SpawnAlertConfig config;
    private final AlertAction chatAction;
    private int soundDelayTicks = 20;

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
        this.chatAction = new ChatAlertAction();
    }

    /**
     * Add a Pokemon to the tracking list and execute initial alert actions.
     *
     * @param entity   the Pokemon entity to track
     * @param priority the alert priority
     */
    public void addTarget(PokemonEntity entity, AlertPriority priority) {
        UUID uuid = entity.getUUID();
        TrackedPokemon tracked = new TrackedPokemon(entity, priority);

        if (mutedUuids.contains(uuid)) {
            tracked.setMuted(true);
        } else {
            // Execute the chat action for new (unmuted) Pokemon
            if (chatAction.isEnabled(config)) {
                chatAction.execute(entity, priority, config);
            }
        }

        trackedPokemon.put(uuid, tracked);

        // Publish event for other systems
        EventBus.publish(new PokemonTrackedEvent(entity, priority));
    }

    /**
     * Get a tracked Pokemon by its entity UUID.
     *
     * @param uuid the entity UUID
     * @return the tracked Pokemon, or null if not found
     */
    public TrackedPokemon getTarget(UUID uuid) {
        return trackedPokemon.get(uuid);
    }

    /**
     * Get all tracked Pokemon UUIDs.
     *
     * @return set of tracked entity UUIDs
     */
    public Set<UUID> getTrackedUuids() {
        return trackedPokemon.keySet();
    }

    /**
     * Get all tracked Pokemon.
     *
     * @return collection of tracked Pokemon
     */
    public Collection<TrackedPokemon> getTrackedPokemon() {
        return trackedPokemon.values();
    }

    /**
     * Mute a specific Pokemon's alert by entity UUID.
     *
     * @param uuid the entity UUID to mute
     */
    public void muteTarget(UUID uuid) {
        mutedUuids.add(uuid);
        TrackedPokemon target = trackedPokemon.get(uuid);
        if (target != null) {
            target.setMuted(true);
        }
        EventBus.publish(new AlertMutedEvent(uuid));
    }

    /**
     * Mute a Pokemon by its actor ID (used when entering battle).
     *
     * @param actorId the Pokemon's actor UUID from battle
     */
    public void muteTargetByActorId(UUID actorId) {
        Optional<Map.Entry<UUID, TrackedPokemon>> targetEntry = trackedPokemon.entrySet().stream()
                .filter(entry -> entry.getValue().getEntity().getPokemon().getUuid().equals(actorId))
                .findFirst();

        if (targetEntry.isPresent()) {
            UUID entityUuid = targetEntry.get().getKey();
            mutedUuids.add(entityUuid);
            targetEntry.get().getValue().setMuted(true);
            EventBus.publish(new AlertMutedEvent(entityUuid));
        }
    }

    /**
     * Check if a Pokemon's alert is muted.
     *
     * @param uuid the entity UUID
     * @return true if muted
     */
    public boolean isTargetMuted(UUID uuid) {
        return mutedUuids.contains(uuid);
    }

    /**
     * Unmute all tracked Pokemon.
     */
    public void unmuteAllTargets() {
        mutedUuids.clear();
        trackedPokemon.values().forEach(t -> t.setMuted(false));
    }

    /**
     * Mute all currently tracked Pokemon.
     */
    public void muteAllTargets() {
        trackedPokemon.forEach((uuid, pokemon) -> {
            mutedUuids.add(uuid);
            pokemon.setMuted(true);
            EventBus.publish(new AlertMutedEvent(uuid));
        });
    }

    /**
     * Check if there are any active (unmuted) targets.
     *
     * @return true if there are unmuted tracked Pokemon
     */
    public boolean hasActiveTarget() {
        return trackedPokemon.values().stream().anyMatch(t -> !t.isMuted());
    }

    /**
     * Clear all tracked Pokemon and mute states.
     */
    public void clearTargets() {
        trackedPokemon.keySet().forEach(uuid -> EventBus.publish(new PokemonUntrackedEvent(uuid)));
        mutedUuids.clear();
        trackedPokemon.clear();
    }

    /**
     * Remove a specific Pokemon from tracking.
     *
     * @param uuid the entity UUID to remove
     */
    public void removeTarget(UUID uuid) {
        if (trackedPokemon.remove(uuid) != null) {
            EventBus.publish(new PokemonUntrackedEvent(uuid));
        }
    }

    /**
     * Process a tick - applies visual effects and plays sounds.
     */
    public void tick() {
        applyEntityHighlights();
        processAlertSounds();
    }

    private void applyEntityHighlights() {
        if (!config.highlightEntity) {
            return;
        }

        for (TrackedPokemon tracked : trackedPokemon.values()) {
            // Sets the glowing flag using the mixin accessor
            ((EntityAccessor) tracked.getEntity()).invokeSetSharedFlag(6, true);
        }
    }

    private void processAlertSounds() {
        if (!config.playSound || config.soundVolume <= 0) {
            return;
        }

        // Find the highest priority among all tracked pokemon that are NOT muted
        AlertPriority highestActive = trackedPokemon.values().stream()
                .filter(t -> !t.isMuted())
                .map(TrackedPokemon::getPriority)
                .max((p1, p2) -> Integer.compare(p1.weight, p2.weight))
                .orElse(AlertPriority.NONE);

        if (highestActive == AlertPriority.NONE) {
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
        if (profile == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        float finalVolume = (config.soundVolume / 100f) * profile.volumeMultiplier();

        SimpleSoundInstance alert = SimpleSoundInstance.forUI(
                profile.sound(),
                profile.pitch(),
                finalVolume);

        mc.getSoundManager().play(alert);
    }
}