package cc.turtl.chiselmon.api.event;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public record PokemonUnloadedEvent(
        PokemonEntity entity,
        boolean isWild
) {
}