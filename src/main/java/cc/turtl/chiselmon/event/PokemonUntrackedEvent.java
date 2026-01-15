package cc.turtl.chiselmon.event;

import java.util.UUID;

/**
 * Event published when a tracked Pokemon is no longer being tracked.
 * This can happen when the entity despawns, the player moves too far away,
 * or the alert is manually dismissed.
 */
public record PokemonUntrackedEvent(UUID entityUuid) implements ChiselmonEvent {
}
