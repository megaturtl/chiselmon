package cc.turtl.cobbleaid.feature.spawnalert;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public class TrackedPokemon {
    final PokemonEntity entity;
    boolean muted = false;

    TrackedPokemon(PokemonEntity entity) {
        this.entity = entity;
    }
}
