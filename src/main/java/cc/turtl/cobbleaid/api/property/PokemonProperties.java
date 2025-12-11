package cc.turtl.cobbleaid.api.property;

import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import cc.turtl.cobbleaid.api.util.CalcUtil;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;

public final class PokemonProperties {
    private PokemonProperties() {
    }

    public static final CustomProperty<Pokemon> IS_SHINY = Pokemon::getShiny;

    public static final CustomProperty<Pokemon> HAS_HIDDEN_ABILITY = pokemon -> {
        if (CalcUtil.countUniqueAbilities(pokemon.getSpecies()) <= 1) {
            return false;
        }
        return new HiddenAbilityProperty(true).matches(pokemon);
    };

    public static final CustomProperty<Pokemon> HAS_HIGH_IVS = pokemon -> {
        IVs ivs = pokemon.getIvs();
        if (ivs == null)
            return false;

        ModConfig config = CobbleAid.getInstance().getConfig();
        if (config == null)
            return false;

        return CalcUtil.countPerfectIVs(ivs) >= config.maxIvsThreshold;
    };

    public static final CustomProperty<Pokemon> IS_EXTREME_SMALL = pokemon -> {
        ModConfig config = CobbleAid.getInstance().getConfig();
        return config != null && pokemon.getScaleModifier() <= config.extremeSmallThreshold;
    };

    public static final CustomProperty<Pokemon> IS_EXTREME_LARGE = pokemon -> {
        ModConfig config = CobbleAid.getInstance().getConfig();
        return config != null && pokemon.getScaleModifier() >= config.extremeLargeThreshold;
    };

    public static final CustomProperty<Pokemon> IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);

    public static final CustomProperty<Pokemon> IS_RIDEABLE = pokemon -> !pokemon.getRiding().getSeats().isEmpty();
}