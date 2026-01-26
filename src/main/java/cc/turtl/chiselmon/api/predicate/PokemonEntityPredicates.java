package cc.turtl.chiselmon.api.predicate;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

public final class PokemonEntityPredicates {
    public static final Predicate<PokemonEntity> IS_OWNED = entity -> entity.getOwnerUUID() != null;
    // some pokedisguises have this tag
    public static final Predicate<PokemonEntity> IS_NO_AI = Mob::isNoAi;
    // Raid or normal boss, usually pokemon can't reach 2x scale
    public static final Predicate<PokemonEntity> IS_BOSS = entity -> (entity.getPokemon().getScaleModifier() >= 2);
    public static final Predicate<PokemonEntity> FROM_POKESNACK = entity -> entity.getAspects().contains("poke_snack_crumbed");
    public static final Predicate<PokemonEntity> IS_WILD = FROM_POKESNACK.or((IS_OWNED.or(IS_BOSS).or(IS_NO_AI)).negate());

    private PokemonEntityPredicates() {
    }
}
