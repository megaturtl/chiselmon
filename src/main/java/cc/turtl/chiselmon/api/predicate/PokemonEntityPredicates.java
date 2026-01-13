package cc.turtl.chiselmon.api.predicate;

import java.util.function.Predicate;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

public final class PokemonEntityPredicates {
    private PokemonEntityPredicates() {
    }

    public static final Predicate<PokemonEntity> IS_OWNED = entity -> entity.getOwnerUUID() != null;

    // Raid or normal boss, usually pokemon can't reach 2x scale
    public static final Predicate<PokemonEntity> IS_BOSS = entity -> {
        return (entity.getPokemon().getScaleModifier() >= 2);
    };

    public static final Predicate<PokemonEntity> IS_WILD = (IS_OWNED.or(IS_BOSS)).negate();
}
