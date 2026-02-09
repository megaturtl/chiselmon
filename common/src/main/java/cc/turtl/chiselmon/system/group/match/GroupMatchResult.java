package cc.turtl.chiselmon.system.group.match;

import cc.turtl.chiselmon.system.group.PokemonGroup;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.Optional;

public record GroupMatchResult(
        Pokemon pokemon,
        Optional<PokemonGroup> highestGroup,
        List<PokemonGroup> allMatches
) {
    public boolean didMatch() {
        return highestGroup.isPresent();
    }
}