package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionPresets;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class GeneralCategory {

    @SerialEntry(comment = "Disable the entire mod")
    public boolean modDisabled = false;

    @SerialEntry(comment = "Custom thresholds for defining pokemon features")
    public Thresholds thresholds = new Thresholds();

    public ConfigCategory build() {
        return ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.general"))
                .option(OptionPresets.tickBox("config.general.mod_disabled", false,
                        () -> modDisabled, v -> modDisabled = v))
                .group(OptionGroup.createBuilder()
                        .name(modTranslatable("config.group.thresholds"))
                        .option(OptionPresets.floatSlider("config.general.extreme_small", 0.3f,
                                () -> thresholds.extremeSmall, v -> thresholds.extremeSmall = v, 0.1f, 0.9f, 0.1f))

                        .option(OptionPresets.floatSlider("config.general.extreme_large", 1.7f,
                                () -> thresholds.extremeLarge, v -> thresholds.extremeLarge = v, 1.1f, 1.9f, 0.1f))

                        .option(OptionPresets.intSlider("config.general.max_ivs", 5,
                                () -> thresholds.maxIvs, v -> thresholds.maxIvs = v, 3, 6, 1))
                        .build())
                .build();
    }

    public static class Thresholds {
        @SerialEntry
        public float extremeSmall = 0.3F;
        @SerialEntry
        public float extremeLarge = 1.7F;
        @SerialEntry
        public int maxIvs = 5;
    }
}
