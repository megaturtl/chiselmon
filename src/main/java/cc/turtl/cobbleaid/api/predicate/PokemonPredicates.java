package cc.turtl.cobbleaid.api.predicate;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import cc.turtl.cobbleaid.api.util.CalcUtil;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;

public final class PokemonPredicates {
    private PokemonPredicates() {
    }

    // Defensive helper to avoid crashes if predicates are invoked before services finish bootstrapping.
    private static ModConfig getConfigSafe() {
        try {
            return CobbleAid.services().config().get();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static final Predicate<Pokemon> IS_SHINY = Pokemon::getShiny;

    public static final Predicate<Pokemon> HAS_HIDDEN_ABILITY = pokemon -> {
        if (CalcUtil.countUniqueAbilities(pokemon.getSpecies()) <= 1) {
            return false;
        }
        return new HiddenAbilityProperty(true).matches(pokemon);
    };

    public static final Predicate<Pokemon> HAS_HIGH_IVS = pokemon -> {
        IVs ivs = pokemon.getIvs();
        if (ivs == null)
            return false;

        ModConfig config = getConfigSafe();
        if (config == null)
            return false;

        return CalcUtil.countPerfectIVs(ivs) >= config.threshold.maxIvs;
    };

    public static final Predicate<Pokemon> IS_EXTREME_SMALL = pokemon -> {
        ModConfig config = getConfigSafe();
        return config != null && pokemon.getScaleModifier() <= config.threshold.extremeSmall;
    };

    public static final Predicate<Pokemon> IS_EXTREME_LARGE = pokemon -> {
        ModConfig config = getConfigSafe();
        return config != null && pokemon.getScaleModifier() >= config.threshold.extremeLarge;
    };

    public static final Predicate<Pokemon> IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);

    public static final Predicate<Pokemon> IS_RIDEABLE = pokemon -> !pokemon.getRiding().getSeats().isEmpty();

    public static final Predicate<Pokemon> HAS_SELF_DAMAGING_MOVE = pokemon -> {
        Set<MoveTemplate> possibleMoves = CalcUtil.getPossibleMoves(pokemon, true);
        List<MoveTemplate> possibleSelfDamagingMoves = possibleMoves.stream()
                .filter(MovePredicates.IS_SELF_DAMAGING)
                .toList();

        return !possibleSelfDamagingMoves.isEmpty();
    };
}
