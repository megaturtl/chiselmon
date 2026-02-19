package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.config.OptionFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GeneralConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public final ThresholdsGroup thresholds = new ThresholdsGroup();
    @SerialEntry
    public boolean modDisabled = false;

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.general"))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.general.mod_disabled",
                        () -> modDisabled,
                        v -> modDisabled = v
                ))
                .option(OptionFactory.keyMappingPicker(
                        "chiselmon.config.general.open_config_keybind",
                        ChiselmonKeybinds.OPEN_CONFIG))
                .group(thresholds.buildGroup())
                .build();
    }

    public static class ThresholdsGroup implements ConfigGroupBuilder {
        private static final float SMALL_MIN = 0.1F, SMALL_MAX = 0.9F, SMALL_STEP = 0.1F;
        private static final float LARGE_MIN = 1.1F, LARGE_MAX = 1.9F, LARGE_STEP = 0.1F;
        private static final int IVS_MIN = 3, IVS_MAX = 6, IVS_STEP = 1;

        @SerialEntry
        public float extremeSmall = 0.3F;
        @SerialEntry
        public float extremeLarge = 1.7F;
        @SerialEntry
        public int maxIvs = 5;

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.group.thresholds"))
                    .option(OptionFactory.floatSlider(
                            "chiselmon.config.general.extreme_small",
                            () -> extremeSmall,
                            v -> extremeSmall = v,
                            SMALL_MIN, SMALL_MAX, SMALL_STEP
                    ))
                    .option(OptionFactory.floatSlider(
                            "chiselmon.config.general.extreme_large",
                            () -> extremeLarge,
                            v -> extremeLarge = v,
                            LARGE_MIN, LARGE_MAX, LARGE_STEP
                    ))
                    .option(OptionFactory.intSlider(
                            "chiselmon.config.general.max_ivs",
                            () -> maxIvs,
                            v -> maxIvs = v,
                            IVS_MIN, IVS_MAX, IVS_STEP
                    ))
                    .build();
        }
    }
}