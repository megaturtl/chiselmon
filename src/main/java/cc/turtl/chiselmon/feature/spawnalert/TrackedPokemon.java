package cc.turtl.chiselmon.feature.spawnalert;

import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

/**
 * Represents a Pokemon entity that is being tracked by the alert system.
 * 
 * <p>This class encapsulates the tracking state for a single Pokemon,
 * including its alert priority and mute status. It provides a clean API
 * for querying and updating tracking state.
 * 
 * <p>Package-private fields are used intentionally to allow AlertManager
 * to efficiently update state while preventing external modification.
 */
public class TrackedPokemon {
    private final PokemonEntity entity;
    private final AlertPriority priority;
    private boolean muted;

    /**
     * Create a new tracked Pokemon.
     *
     * @param entity   the Pokemon entity to track
     * @param priority the alert priority for this Pokemon
     */
    TrackedPokemon(PokemonEntity entity, AlertPriority priority) {
        this.entity = entity;
        this.priority = priority;
        this.muted = false;
    }

    /**
     * Get the tracked Pokemon entity.
     *
     * @return the Pokemon entity
     */
    public PokemonEntity getEntity() {
        return entity;
    }

    /**
     * Get the entity's UUID.
     *
     * @return the entity UUID
     */
    public UUID getUuid() {
        return entity.getUUID();
    }

    /**
     * Get the alert priority for this Pokemon.
     *
     * @return the alert priority
     */
    public AlertPriority getPriority() {
        return priority;
    }

    /**
     * Check if this Pokemon's alert is muted.
     *
     * @return true if muted
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Set the muted state for this Pokemon.
     * Package-private to restrict mutation to AlertManager.
     *
     * @param muted the new muted state
     */
    void setMuted(boolean muted) {
        this.muted = muted;
    }
}
