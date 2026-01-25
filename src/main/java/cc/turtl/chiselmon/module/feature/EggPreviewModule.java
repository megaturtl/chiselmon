package cc.turtl.chiselmon.module.feature;

import cc.turtl.chiselmon.feature.eggpreview.EggPreviewFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;

public class EggPreviewModule implements ChiselmonModule {
    private final EggPreviewFeature feature;

    public EggPreviewModule(EggPreviewFeature feature) {
        this.feature = feature;
    }

    @Override
    public String id() {
        return "egg-preview";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }
}
