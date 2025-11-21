package cc.turtl.cobbleaid.features.pc;

import cc.turtl.cobbleaid.CobbleAidClient;
import cc.turtl.cobbleaid.config.ModConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.api.filter.PokemonFilterCondition;
import cc.turtl.cobbleaid.util.ColorLibrary;

public final class PcHighlight {

    public static Integer getHighlightColor(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }

        ModConfig config = (CobbleAidClient.getInstance().getConfig());
        if (config == null || config.modDisabled) {
            return null;
        }

        ModConfig.PcHighlighting highlightingConfig = config.pcHighlighting;

        Integer rgbColor = null;

        if (highlightingConfig.highlightHighIV
                && PokemonFilterCondition.hasHighIVs.matches(pokemon)) {
            rgbColor = highlightingConfig.highIVColor;
        } else if (highlightingConfig.highlightExtremeSmall
                && PokemonFilterCondition.isExtremeSmall.matches(pokemon)) {
            rgbColor = highlightingConfig.extremeSmallColor;
        } else if (highlightingConfig.highlightExtremeLarge
                && PokemonFilterCondition.isExtremeLarge.matches(pokemon)) {
            rgbColor = highlightingConfig.extremeLargeColor;
        } else if (highlightingConfig.highlightHiddenAbility
                && PokemonFilterCondition.hasHiddenAbility.matches(pokemon)) {
            rgbColor = highlightingConfig.hiddenAbilityColor;
        } else if (highlightingConfig.highlightShiny
                && PokemonFilterCondition.isShiny.matches(pokemon)) {
            rgbColor = highlightingConfig.shinyColor;
        }

        return rgbColor == null ? null : ColorLibrary.withAlpha(rgbColor, highlightingConfig.highlightAlpha);
    }
}
