package cc.turtl.chiselmon.event;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;

/**
 * Event published when a wild Pokemon is detected that matches alert criteria.
 * 
 * <p>This event is published by the SpawnAlert detection system and can be
 * consumed by any component that needs to react to interesting Pokemon spawns
 * (e.g., alert sounds, chat messages, HUD indicators, logging systems).
 */
public record PokemonTrackedEvent(
        PokemonEntity entity,
        AlertPriority priority) implements ChiselmonEvent {
}
