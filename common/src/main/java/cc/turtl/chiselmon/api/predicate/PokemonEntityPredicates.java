package cc.turtl.chiselmon.api.predicate;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Predicate;

public final class PokemonEntityPredicates {
    public static final Predicate<PokemonEntity> IS_OWNED = entity -> entity.getOwnerUUID() != null;
    public static final Predicate<PokemonEntity> FROM_POKESNACK = entity -> entity.getAspects().contains("poke_snack_crumbed");

    /**
     *  WARNING: This predicate relies on several assumptions of behavior:
     * - Plushies are always level 1
     * - Plushies always have noai = true when freshly placed
     * - Plushies always have max_health = 20 when not freshly placed
     */
    public static final Predicate<PokemonEntity> IS_WILD = entity -> {
        if (IS_OWNED.test(entity)) return false; // Highest priority. If pokemon is owned NEVER consider it wild.
        if (FROM_POKESNACK.test(entity)) return true; // If from a pokesnack it must be wild.
        if (entity.isNoAi()) return false; // If noai, it might be a pokedisguise or a recently placed plushie.
        if (entity.getPokemon().getScaleModifier() >= 2) return false; // If larger than 2.0 scale, it is probably a raid boss.
        if (entity.getPokemon().getLevel() > 1) return true; // At this point we let all non level 1s pass.

        // Wild level 1s may have health modifiers applied, plushies will always be exactly at base (20.0) if old and at 6.0 if freshly placed
        AttributeInstance max_health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (max_health != null && max_health.getValue() != max_health.getBaseValue()) return true;

        // By now it's probably a plushie
        return false;
    };

    private PokemonEntityPredicates() {
    }
}
