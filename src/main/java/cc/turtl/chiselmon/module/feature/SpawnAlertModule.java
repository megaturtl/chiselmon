package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class SpawnAlertModule implements ChiselmonModule {
    private final SpawnAlertFeature feature = new SpawnAlertFeature();

    public SpawnAlertFeature feature() {
        return feature;
    }

    @Override
    public String id() {
        return "spawn-alert";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }
}
