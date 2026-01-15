package cc.turtl.chiselmon.feature.spawnalert.condition;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Alert condition that triggers for extreme-sized Pokemon.
 * This condition bypasses the blacklist - extreme sizes are always alerted if enabled.
 */
public class ExtremeSizeCondition implements AlertCondition {

    @Override
    public AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config) {
        if (!config.alertOnExtremeSize) {
            return AlertPriority.NONE;
        }

        Pokemon pokemon = entity.getPokemon();
        return PokemonPredicates.IS_EXTREME_SIZE.test(pokemon) ? AlertPriority.SIZE : AlertPriority.NONE;
    }
}
