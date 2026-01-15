package cc.turtl.chiselmon.feature.spawnalert.action;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;

/**
 * Interface for actions that execute when a Pokemon alert is triggered.
 * 
 * <p>This follows the Command pattern, allowing different alert behaviors
 * to be composed and configured independently. Actions can include:
 * <ul>
 *   <li>Sending chat messages</li>
 *   <li>Playing sounds</li>
 *   <li>Highlighting entities</li>
 *   <li>Logging to files</li>
 *   <li>Displaying HUD notifications</li>
 * </ul>
 * 
 * <h2>Lifecycle:</h2>
 * <p>Actions may be called multiple times for the same Pokemon if needed
 * (e.g., sound actions may be called on each tick while active).
 * 
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class ChatAlertAction implements AlertAction {
 *     @Override
 *     public boolean isEnabled(SpawnAlertConfig config) {
 *         return config.sendChatMessage;
 *     }
 *     
 *     @Override
 *     public void execute(PokemonEntity entity, AlertPriority priority, SpawnAlertConfig config) {
 *         // Send chat message
 *     }
 * }
 * }</pre>
 */
public interface AlertAction {

    /**
     * Check if this action is enabled in the current configuration.
     *
     * @param config the current spawn alert configuration
     * @return true if the action should execute
     */
    boolean isEnabled(SpawnAlertConfig config);

    /**
     * Execute the alert action for a Pokemon.
     *
     * @param entity   the Pokemon entity that triggered the alert
     * @param priority the priority level of the alert
     * @param config   the current spawn alert configuration
     */
    void execute(PokemonEntity entity, AlertPriority priority, SpawnAlertConfig config);
}
