package cc.turtl.chiselmon.api.event;

import cc.turtl.chiselmon.api.PokemonEncounter;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public record PokemonLoadedEvent(
        PokemonEntity entity,
        PokemonEncounter encounterSnapshot,
        boolean isWild
) {
}