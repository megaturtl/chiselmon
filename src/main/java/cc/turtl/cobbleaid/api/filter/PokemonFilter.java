package cc.turtl.cobbleaid.api.filter;

import com.cobblemon.mod.common.pokemon.Pokemon;

@FunctionalInterface
public interface PokemonFilter {
    boolean matches(Pokemon pokemon);

    default PokemonFilter and(PokemonFilter other) {
        return pokemon -> this.matches(pokemon) && other.matches(pokemon);
    }

    default PokemonFilter or(PokemonFilter other) {
        return pokemon -> this.matches(pokemon) || other.matches(pokemon);
    }
    
    static PokemonFilter not(PokemonFilter filter) {
        return pokemon -> !filter.matches(pokemon);
    }
}
