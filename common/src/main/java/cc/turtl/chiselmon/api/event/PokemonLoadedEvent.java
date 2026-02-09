package cc.turtl.chiselmon.api.event;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public record PokemonLoadedEvent(
        PokemonEntity entity,
        boolean isWild
) {}