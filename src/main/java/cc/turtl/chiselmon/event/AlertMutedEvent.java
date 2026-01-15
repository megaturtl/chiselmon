package cc.turtl.chiselmon.event;

import java.util.UUID;

/**
 * Event published when an alert for a specific Pokemon is muted.
 * This allows multiple systems to respond to muting (e.g., stop sounds, update UI).
 */
public record AlertMutedEvent(UUID entityUuid) implements ChiselmonEvent {
}
