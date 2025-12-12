package cc.turtl.cobbleaid.core.lifecycle;

/**
 * Interface for modular features in CobbleAid.
 * <p>
 * Each feature should implement this interface to provide a consistent
 * lifecycle and enable/disable mechanism. Features are initialized during
 * mod startup and can be toggled via configuration.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * public class MyFeature implements Feature {
 *     {@literal @}Override
 *     public void initialize() {
 *         // Register event listeners, commands, etc.
 *     }
 *     
 *     {@literal @}Override
 *     public boolean isEnabled() {
 *         return CobbleAid.getInstance().getConfig().myFeature.enabled;
 *     }
 * }
 * </pre>
 * </p>
 */
public interface Feature {
    
    /**
     * Initialize the feature. Called once during mod initialization.
     * This is where you should register event listeners, commands,
     * keybindings, and other static resources.
     */
    void initialize();
    
    /**
     * Check if this feature is currently enabled based on configuration.
     * This should be checked at runtime before executing feature logic.
     * 
     * @return true if the feature is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Get a human-readable name for this feature.
     * Used for logging and debugging purposes.
     * 
     * @return the feature name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
