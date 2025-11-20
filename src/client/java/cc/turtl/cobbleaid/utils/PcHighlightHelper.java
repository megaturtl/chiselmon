package cc.turtl.cobbleaid.utils;

import cc.turtl.cobbleaid.CobbleAidClient;
import cc.turtl.cobbleaid.config.ModConfig;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import java.util.Map;

public final class PcHighlightHelper {
    private static final HiddenAbilityProperty HIDDEN_ABILITY_PROPERTY = new HiddenAbilityProperty(true);

    private PcHighlightHelper() {}

    public static Integer getHighlightColor(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }

        CobbleAidClient client = CobbleAidClient.getInstance();
        if (client == null) {
            return null;
        }

        ModConfig config = client.getConfig();
        if (config == null || config.modDisabled) {
            return null;
        }

        ModConfig.PcHighlighting highlighting = config.pcHighlighting;
        if (highlighting == null) {
            return null;
        }

        if (highlighting.highlightHiddenAbility && hasHiddenAbility(pokemon)) {
            return highlighting.hiddenAbilityColor;
        }

        if (highlighting.highlightMaxIV && hasPerfectIVs(pokemon)) {
            return highlighting.maxIvColor;
        }

        if (highlighting.highlightShiny && pokemon.getShiny()) {
            return highlighting.shinyColor;
        }

        return null;
    }

    private static boolean hasHiddenAbility(Pokemon pokemon) {
        return HIDDEN_ABILITY_PROPERTY.matches(pokemon);
    }

    private static boolean hasPerfectIVs(Pokemon pokemon) {
        IVs ivs = pokemon.getIvs();
        if (ivs == null) {
            return false;
        }

        for (Map.Entry<? extends Stat, ? extends Integer> entry : ivs) {
            Integer value = entry.getValue();
            if (value == null || value != IVs.MAX_VALUE) {
                return false;
            }
        }

        return true;
    }
}

