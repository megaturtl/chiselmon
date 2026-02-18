package cc.turtl.chiselmon.api.predicate;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

public final class PokemonEntityPredicates {
    public static final Predicate<PokemonEntity> IS_OWNED = entity -> entity.getOwnerUUID() != null;
    // some pokedisguises have this tag, but this doesn't filter all of them ;(
    public static final Predicate<PokemonEntity> IS_DISGUISE = Mob::isNoAi;
    // Raid or normal boss, usually pokemon can't reach 2x scale
    public static final Predicate<PokemonEntity> IS_BOSS = entity -> (entity.getPokemon().getScaleModifier() >= 2);
    // Necessary evil to just block lvl 1s for now i think
    public static final Predicate<PokemonEntity> IS_PLUSHIE = entity -> (entity.getPokemon().getLevel() <= 1);
    public static final Predicate<PokemonEntity> FROM_POKESNACK = entity -> entity.getAspects().contains("poke_snack_crumbed");
    public static final Predicate<PokemonEntity> IS_WILD = FROM_POKESNACK.or((IS_OWNED.or(IS_BOSS).or(IS_DISGUISE).or(IS_PLUSHIE)).negate());

    private PokemonEntityPredicates() {
    }
}
