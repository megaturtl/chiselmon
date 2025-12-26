package cc.turtl.cobbleaid.feature;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;

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
        CobbleAid.getLogger().debug("Feature [{}] initialized.", featureName);
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
        return !CobbleAid.isDisabled() && isFeatureEnabled();
    }

    /**
     * Each feature defines its own config check here.
     */
    protected abstract boolean isFeatureEnabled();

    protected ModConfig getConfig() {
        return CobbleAid.services().config().get();
    }
}