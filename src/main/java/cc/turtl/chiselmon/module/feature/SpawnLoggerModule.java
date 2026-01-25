package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class SpawnLoggerModule implements ChiselmonModule {
    public static final String ID = "spawn-logger";
    private final SpawnLoggerFeature feature = new SpawnLoggerFeature();

    public SpawnLoggerFeature feature() {
        return feature;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void initialize() {
        feature.initialize();
    }
}
