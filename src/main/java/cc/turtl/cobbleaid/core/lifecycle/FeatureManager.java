package cc.turtl.cobbleaid.core.lifecycle;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final Map<Class<? extends Feature>, Feature> featuresByType = new HashMap<>();
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
        featuresByType.put(feature.getClass(), feature);
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
    
    /**
     * Get a feature by its class type.
     * 
     * @param <T> the feature type
     * @param featureClass the class of the feature
     * @return an Optional containing the feature if found
     */
    @SuppressWarnings("unchecked")
    public <T extends Feature> Optional<T> getFeature(Class<T> featureClass) {
        return Optional.ofNullable((T) featuresByType.get(featureClass));
    }
    
    /**
     * Check if a feature is enabled.
     * Returns false if the feature is not registered.
     * 
     * @param featureClass the class of the feature
     * @return true if the feature is registered and enabled
     */
    public boolean isFeatureEnabled(Class<? extends Feature> featureClass) {
        return getFeature(featureClass)
            .map(Feature::isEnabled)
            .orElse(false);
    }
}
