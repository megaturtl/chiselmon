package cc.turtl.cobbleaid.api.filter;

import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;
import cc.turtl.cobbleaid.api.StatCalculator;

import cc.turtl.cobbleaid.CobbleAidClient;
import cc.turtl.cobbleaid.config.ModConfig;

public class PokemonFilterCondition {
    public static final PokemonFilter isShiny = Pokemon::getShiny;

    public static final PokemonFilter hasHiddenAbility = pokemon -> {
        final HiddenAbilityProperty HIDDEN_ABILITY_PROPERTY = new HiddenAbilityProperty(true);
        return HIDDEN_ABILITY_PROPERTY.matches(pokemon);
    };

    public static final PokemonFilter hasHighIVs = pokemon -> {
        IVs ivs = pokemon.getIvs();
        if (ivs == null)
            return false;

        ModConfig config = (CobbleAidClient.getInstance().getConfig());
        if (config == null || config.modDisabled) {
            return false;
        }

        int IVThreshold = config.highIVTotalThreshold;
        int IVTotal = StatCalculator.calculateTotalIVs(pokemon);
        
        return IVTotal >= IVThreshold;
    };

    public static final PokemonFilter isExtremeSmall = pokemon -> {
        ModConfig config = (CobbleAidClient.getInstance().getConfig());
        if (config == null || config.modDisabled) {
            return false;
        }

        float scale = pokemon.getScaleModifier();

        if (scale <= config.extremeSmallThreshold) {
            return true;
        }
        return false;
    };

    public static final PokemonFilter isExtremeLarge = pokemon -> {
        ModConfig config = (CobbleAidClient.getInstance().getConfig());
        if (config == null || config.modDisabled) {
            return false;
        }

        float scale = pokemon.getScaleModifier();

        if (scale >= config.extremeLargeThreshold) {
            return true;
        }
        return false;
    };

}
