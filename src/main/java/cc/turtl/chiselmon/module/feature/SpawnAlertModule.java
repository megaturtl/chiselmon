package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class SpawnAlertModule implements ChiselmonModule {
    public static final String ID = "spawn-alert";
    private final SpawnAlertFeature feature = new SpawnAlertFeature();

    public SpawnAlertFeature feature() {
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
