package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class CheckSpawnTrackerModule implements ChiselmonModule {
    private final CheckSpawnTrackerFeature feature = new CheckSpawnTrackerFeature();

    public CheckSpawnTrackerFeature feature() {
        return feature;
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
