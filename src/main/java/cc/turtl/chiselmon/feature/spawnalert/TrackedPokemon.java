package cc.turtl.chiselmon.feature.spawnalert;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class TrackedPokemon {
    final PokemonEntity entity;
    boolean muted = false;
    AlertPriority priority = AlertPriority.NONE;

    TrackedPokemon(PokemonEntity entity) {
        this.entity = entity;
    }
}
