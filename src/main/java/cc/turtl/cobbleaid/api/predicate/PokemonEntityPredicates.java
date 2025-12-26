package cc.turtl.cobbleaid.api.predicate;

import java.util.function.Predicate;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public final class PokemonEntityPredicates {
    private PokemonEntityPredicates() {
    }

    public static final Predicate<PokemonEntity> IS_OWNED = entity -> entity.getOwnerUUID() != null;

    // this is the best i can do rn but needs to be improved
    public static final Predicate<PokemonEntity> IS_PLUSHIE = entity -> {
        return (entity.getPokemon().getLevel() == 1 && entity.getPokemon().getExperience() == 0);
    };
}
