package cc.turtl.cobbleaid.api.filter;

import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import cc.turtl.cobbleaid.api.util.CalcUtil;
import cc.turtl.cobbleaid.api.util.IVsUtil;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;

public interface PokemonConditions {
    public static final PokemonFilter IS_SHINY = Pokemon::getShiny;

    public static final PokemonFilter HAS_HIDDEN_ABILITY = pokemon -> {
        int potentialAbilities = CalcUtil.getSpeciesAbilityCount(pokemon.getSpecies());

        if (potentialAbilities <= 1) {
            return false;
        }
        return new HiddenAbilityProperty(true).matches(pokemon);
    };

    public static final PokemonFilter HAS_HIGH_IVS = pokemon -> {
        IVs ivs = pokemon.getIvs();
        if (ivs == null)
            return false;

        ModConfig config = (CobbleAid.getInstance().getConfig());
        if (config == null || config.modDisabled) {
            return false;
        }

        int IVThreshold = config.highIVTotalThreshold;
        int IVTotal = IVsUtil.calculateTotalIVs(ivs);
        
        return IVTotal >= IVThreshold;
    };

    public static final PokemonFilter IS_EXTREME_SMALL = pokemon -> {
        ModConfig config = CobbleAid.getInstance().getConfig();
        if (config == null || config.modDisabled) {
            return false;
        }
        
        float scale = pokemon.getScaleModifier();
        return scale <= config.extremeSmallThreshold;
    };
    
    public static final PokemonFilter IS_EXTREME_LARGE = pokemon -> {
        ModConfig config = CobbleAid.getInstance().getConfig();
        if (config == null || config.modDisabled) {
            return false;
        }
        
        float scale = pokemon.getScaleModifier();
        return scale >= config.extremeLargeThreshold;
    };
    
    public static final PokemonFilter IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);
}
