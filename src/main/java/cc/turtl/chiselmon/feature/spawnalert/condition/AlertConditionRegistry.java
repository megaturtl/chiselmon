package cc.turtl.chiselmon.feature.spawnalert.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Registry for alert conditions. Conditions are evaluated in priority order
 * (highest first) and the first matching condition determines the alert priority.
 * 
 * <p>This registry allows dynamic registration of conditions, enabling:
 * <ul>
 *   <li>Built-in conditions to be registered at startup</li>
 *   <li>Third-party addons to register custom conditions</li>
 *   <li>Conditions to be enabled/disabled without code changes</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Register a condition (typically done during feature initialization)
 * AlertConditionRegistry.register(new ShinyCondition());
 * 
 * // Evaluate all conditions for a Pokemon
 * AlertPriority priority = AlertConditionRegistry.evaluate(pokemonEntity, config);
 * }</pre>
 */
public final class AlertConditionRegistry {
    private static final Logger LOGGER = LogManager.getLogger("chiselmon");
    private static final List<AlertCondition> CONDITIONS = new ArrayList<>();

    private AlertConditionRegistry() {
    }

    /**
     * Register a new alert condition.
     * Conditions are evaluated in registration order, with all conditions
     * being checked and the highest priority result being returned.
     *
     * @param condition the condition to register
     */
    public static void register(AlertCondition condition) {
        CONDITIONS.add(condition);
        LOGGER.debug("AlertConditionRegistry: Registered {}", condition.getClass().getSimpleName());
    }

    /**
     * Register multiple conditions at once.
     *
     * @param conditions the conditions to register
     */
    public static void registerAll(AlertCondition... conditions) {
        for (AlertCondition condition : conditions) {
            register(condition);
        }
    }

    /**
     * Evaluate all registered conditions against a Pokemon entity.
     * Returns the highest priority that matches.
     *
     * @param entity the Pokemon entity to evaluate
     * @param config the current spawn alert configuration
     * @return the highest matching priority, or {@link AlertPriority#NONE}
     */
    public static AlertPriority evaluate(PokemonEntity entity, SpawnAlertConfig config) {
        AlertPriority highest = AlertPriority.NONE;

        for (AlertCondition condition : CONDITIONS) {
            AlertPriority result = condition.evaluate(entity, config);
            if (result.weight > highest.weight) {
                highest = result;
            }
        }

        return highest;
    }

    /**
     * Get an unmodifiable view of registered conditions.
     * Useful for debugging and testing.
     *
     * @return list of registered conditions
     */
    public static List<AlertCondition> getConditions() {
        return Collections.unmodifiableList(CONDITIONS);
    }

    /**
     * Clear all registered conditions. Use with caution - typically only for testing.
     */
    public static void clear() {
        CONDITIONS.clear();
    }
}
