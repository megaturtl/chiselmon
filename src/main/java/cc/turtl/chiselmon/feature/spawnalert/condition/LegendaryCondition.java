package cc.turtl.chiselmon.feature.spawnalert.condition;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Alert condition that triggers for legendary, mythical, ultra beast, and paradox Pokemon.
 * This condition respects the blacklist.
 */
public class LegendaryCondition implements AlertCondition {

    @Override
    public AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config) {
        Pokemon pokemon = entity.getPokemon();

        // Check if any of the legendary-type categories are enabled and match
        boolean isLegendaryType = (config.alertOnLegendary
                && (PokemonPredicates.IS_LEGENDARY.test(pokemon) || PokemonPredicates.IS_MYTHICAL.test(pokemon)))
                || (config.alertOnUltraBeast && PokemonPredicates.IS_ULTRABEAST.test(pokemon))
                || (config.alertOnParadox && PokemonPredicates.IS_PARADOX.test(pokemon));

        if (!isLegendaryType) {
            return AlertPriority.NONE;
        }

        // Check blacklist
        if (PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
            return AlertPriority.NONE;
        }

        return AlertPriority.LEGENDARY;
    }
}
