package cc.turtl.chiselmon.feature;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.config.ModConfig;

public abstract class AbstractFeature {
    protected final String featureName;

    protected AbstractFeature(String featureName) {
        this.featureName = featureName;
    }

    /**
     * The entry point called by the main class.
     */
    public final void initialize() {
        init();
        Chiselmon.getLogger().debug("Feature [{}] initialized.", featureName);
    }

    /**
     * Each feature implements its own setup logic here.
     * Could be listeners, commands, or nothing at all.
     */
    protected abstract void init();

    /**
     * Centralized check for whether this specific feature should run.
     * Checks both the global mod toggle AND the feature-specific toggle.
     */
    public boolean canRun() {
        return !Chiselmon.isDisabled() && isFeatureEnabled();
    }

    /**
     * Each feature defines its own config check here.
     */
    protected abstract boolean isFeatureEnabled();

    protected ModConfig getConfig() {
        return Chiselmon.services().config().get();
    }
}