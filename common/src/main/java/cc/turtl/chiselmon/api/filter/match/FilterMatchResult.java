package cc.turtl.chiselmon.api.filter.match;

import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.Optional;

public record FilterMatchResult(
        Pokemon pokemon,
        Optional<RuntimeFilter> primaryMatch,
        List<RuntimeFilter> allMatches
) {
}