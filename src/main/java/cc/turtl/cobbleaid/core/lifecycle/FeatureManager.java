package cc.turtl.cobbleaid.core.lifecycle;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of all features in CobbleAid.
 * <p>
 * This class is responsible for registering and initializing features
 * in a consistent manner. Features are initialized in the order they
 * are registered.
 * </p>
 */
public class FeatureManager {
    
    private final List<Feature> features = new ArrayList<>();
    private final CobbleAidLogger logger;
    private boolean initialized = false;
    
    public FeatureManager(CobbleAidLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Register a feature with the manager.
     * Must be called before {@link #initializeAll()}.
     * 
     * @param feature the feature to register
     * @throws IllegalStateException if called after initialization
     */
    public void register(Feature feature) {
        if (initialized) {
            throw new IllegalStateException(
                "Cannot register features after initialization. " +
                "Attempted to register: " + feature.getName()
            );
        }
        features.add(feature);
        logger.debug("Registered feature: {}", feature.getName());
    }
    
    /**
     * Initialize all registered features.
     * Should be called once during mod initialization.
     */
    public void initializeAll() {
        if (initialized) {
            logger.warn("FeatureManager already initialized, skipping.");
            return;
        }
        
        logger.info("Initializing {} features...", features.size());
        
        for (Feature feature : features) {
            try {
                logger.debug("Initializing feature: {}", feature.getName());
                feature.initialize();
                logger.debug("Feature initialized: {}", feature.getName());
            } catch (Exception e) {
                logger.error("Failed to initialize feature: {}", feature.getName(), e);
            }
        }
        
        initialized = true;
        logger.info("Feature initialization complete.");
    }
    
    /**
     * Get all registered features.
     * 
     * @return an unmodifiable list of features
     */
    public List<Feature> getFeatures() {
        return List.copyOf(features);
    }
    
    /**
     * Get the count of registered features.
     * 
     * @return the number of registered features
     */
    public int getFeatureCount() {
        return features.size();
    }
}
