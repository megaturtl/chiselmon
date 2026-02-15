package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class GeneralConfig implements ConfigCategoryBuilder {

    @SerialEntry(comment = "Custom thresholds for defining pokemon features")
    public final ThresholdsGroup thresholds = new ThresholdsGroup();
    @SerialEntry(comment = "Disable the entire mod")
    public boolean modDisabled = false;

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.general"))
                .option(OptionFactory.toggleOnOff(
                        "config.general.mod_disabled",
                        () -> modDisabled,
                        v -> modDisabled = v
                ))
                .group(thresholds.buildGroup())
                .build();
    }

    public static class ThresholdsGroup implements ConfigGroupBuilder {
        private static final float SMALL_MIN = 0.1F, SMALL_MAX = 0.9F, SMALL_STEP = 0.1F;
        private static final float LARGE_MIN = 1.1F, LARGE_MAX = 1.9F, LARGE_STEP = 0.1F;
        private static final int IVS_MIN = 3, IVS_MAX = 6, IVS_STEP = 1;

        @SerialEntry(comment = "Minimum size threshold for 'extremely small' pokemon")
        public float extremeSmall = 0.3F;
        @SerialEntry(comment = "Minimum size threshold for 'extremely large' pokemon")
        public float extremeLarge = 1.7F;
        @SerialEntry(comment = "Number of max IVs required to be considered high IVs")
        public int maxIvs = 5;

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(modTranslatable("config.group.thresholds"))
                    .option(OptionFactory.floatSlider(
                            "config.general.extreme_small",
                            () -> extremeSmall,
                            v -> extremeSmall = v,
                            SMALL_MIN, SMALL_MAX, SMALL_STEP
                    ))
                    .option(OptionFactory.floatSlider(
                            "config.general.extreme_large",
                            () -> extremeLarge,
                            v -> extremeLarge = v,
                            LARGE_MIN, LARGE_MAX, LARGE_STEP
                    ))
                    .option(OptionFactory.intSlider(
                            "config.general.max_ivs",
                            () -> maxIvs,
                            v -> maxIvs = v,
                            IVS_MIN, IVS_MAX, IVS_STEP
                    ))
                    .build();
        }
    }
}