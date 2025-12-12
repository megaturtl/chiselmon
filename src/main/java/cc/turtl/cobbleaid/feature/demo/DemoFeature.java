package cc.turtl.cobbleaid.feature.demo;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

/**
 * A minimal demo feature showcasing the modular architecture.
 * <p>
 * This feature serves as an example for developers on how to:
 * <ul>
 *   <li>Implement the {@link Feature} interface</li>
 *   <li>Initialize feature resources</li>
 *   <li>Check feature enablement via configuration</li>
 *   <li>Use the logger for debugging</li>
 * </ul>
 * </p>
 * <p>
 * To add a new feature to CobbleAid:
 * <ol>
 *   <li>Create a new package under {@code cc.turtl.cobbleaid.feature.<featurename>}</li>
 *   <li>Create a Feature implementation class like this one</li>
 *   <li>Add configuration options in {@code ModConfig} if needed</li>
 *   <li>Register the feature in {@code CobbleAid.registerFeatures()}</li>
 *   <li>Add any required mixins in the appropriate mixin package</li>
 * </ol>
 * </p>
 * 
 * @see Feature
 * @see cc.turtl.cobbleaid.core.lifecycle.FeatureManager
 */
public class DemoFeature implements Feature {
    
    private static final CobbleAidLogger LOGGER = CobbleAid.getLogger();
    
    @Override
    public void initialize() {
        // This method is called once during mod initialization
        // Register event listeners, commands, keybindings here
        LOGGER.debug("DemoFeature initialized - This is an example feature!");
        
        // Example: Register a command
        // CobbleAid.getInstance().getRegistryHelper()
        //     .executeRegistration("demo-command", () -> {
        //         // Command registration logic
        //     });
    }
    
    @Override
    public boolean isEnabled() {
        // Check configuration to determine if feature is enabled
        // For this demo, it's always disabled (no config entry)
        return false;
    }
    
    @Override
    public String getName() {
        return "Demo Feature";
    }
}
