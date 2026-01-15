package cc.turtl.chiselmon.feature.spawnalert.condition;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Alert condition that triggers for Pokemon on the user's custom whitelist.
 * This condition respects the blacklist.
 */
public class CustomListCondition implements AlertCondition {

    @Override
    public AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config) {
        if (!config.alertOnCustomList) {
            return AlertPriority.NONE;
        }

        Pokemon pokemon = entity.getPokemon();

        // Check whitelist
        if (!PokemonPredicates.isInCustomList(config.whitelist).test(pokemon)) {
            return AlertPriority.NONE;
        }

        // Check blacklist
        if (PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
            return AlertPriority.NONE;
        }

        return AlertPriority.CUSTOM;
    }
}
