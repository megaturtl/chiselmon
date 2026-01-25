package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class CheckSpawnTrackerModule implements ChiselmonModule {
    private final CheckSpawnTrackerFeature feature;

    public CheckSpawnTrackerModule(CheckSpawnTrackerFeature feature) {
        this.feature = feature;
    }

    @Override
    public String id() {
        return "check-spawn-tracker";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }
}
