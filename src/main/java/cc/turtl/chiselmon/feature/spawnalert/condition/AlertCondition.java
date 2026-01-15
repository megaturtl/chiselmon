package cc.turtl.chiselmon.feature.spawnalert.condition;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Interface for alert conditions that determine when a Pokemon should trigger an alert.
 * 
 * <p>This follows the Strategy pattern, allowing new alert conditions to be added
 * without modifying existing code. Each condition is responsible for:
 * <ul>
 *   <li>Checking if it's enabled in the configuration</li>
 *   <li>Testing if a Pokemon matches its criteria</li>
 *   <li>Returning the appropriate priority level</li>
 * </ul>
 * 
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class ShinyCondition implements AlertCondition {
 *     @Override
 *     public AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config) {
 *         if (!config.alertOnShiny) {
 *             return AlertPriority.NONE;
 *         }
 *         return entity.getPokemon().getShiny() ? AlertPriority.SHINY : AlertPriority.NONE;
 *     }
 * }
 * }</pre>
 * 
 * <h2>Extensibility:</h2>
 * <p>Third-party addons or future features can create new conditions by implementing
 * this interface and registering them with the {@link AlertConditionRegistry}.
 */
@FunctionalInterface
public interface AlertCondition {
    
    /**
     * Evaluate whether the given Pokemon entity should trigger an alert.
     *
     * @param entity the Pokemon entity to evaluate
     * @param config the current spawn alert configuration
     * @return the priority of the alert, or {@link AlertPriority#NONE} if no alert
     */
    AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config);
}
