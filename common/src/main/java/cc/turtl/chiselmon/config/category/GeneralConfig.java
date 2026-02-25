package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.config.OptionFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GeneralConfig {
    public static final boolean DEFAULT_MOD_DISABLED = false;
    public static final boolean DEFAULT_MOVE_DETAIL = true;
    public static final boolean DEFAULT_CHECKSPAWN_DETAIL = true;

    @SerialEntry
    public boolean modDisabled = DEFAULT_MOD_DISABLED;

    @SerialEntry
    public boolean moveDetail = DEFAULT_MOVE_DETAIL;

    @SerialEntry
    public boolean checkSpawnDetail = DEFAULT_CHECKSPAWN_DETAIL;

    @SerialEntry
    public String discordWebhookURL = "";

    @SerialEntry
    public final EggSpyConfig eggSpy = new EggSpyConfig();

    @SerialEntry
    public final ThresholdsConfig thresholds = new ThresholdsConfig();

    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.general"))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.general.mod_disabled",
                        DEFAULT_MOD_DISABLED,
                        () -> modDisabled,
                        v -> modDisabled = v
                ))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.general.move_detail",
                        DEFAULT_MOVE_DETAIL,
                        () -> moveDetail,
                        v -> moveDetail = v
                ))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.general.checkspawn_detail",
                        DEFAULT_CHECKSPAWN_DETAIL,
                        () -> checkSpawnDetail,
                        v -> checkSpawnDetail = v
                ))
                .option(OptionFactory.textField(
                        "chiselmon.config.general.discord_webhook_url",
                        "",
                        () -> discordWebhookURL,
                        v -> discordWebhookURL = v
                ))
                .option(OptionFactory.keyMappingPicker(
                        "chiselmon.config.general.open_config_keybind",
                        ChiselmonKeybinds.OPEN_CONFIG))
                .group(eggSpy.buildGroup())
                .group(thresholds.buildGroup())
                .build();
    }

    public static class EggSpyConfig {
        public static final boolean DEFAULT_ENABLED = false;
        public static final boolean DEFAULT_SHOW_HATCH_OVERLAY = false;

        @SerialEntry
        public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry
        public boolean showHatchOverlay = DEFAULT_SHOW_HATCH_OVERLAY;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.group.egg_spy"))
                    .option(OptionFactory.toggleOnOff(
                            "chiselmon.config.egg_spy.enabled",
                            DEFAULT_ENABLED,
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.egg_spy.hatch_overlay",
                            DEFAULT_SHOW_HATCH_OVERLAY,
                            () -> showHatchOverlay,
                            v -> showHatchOverlay = v
                    ))
                    .build();
        }
    }

    public static class ThresholdsConfig {
        public static final float DEFAULT_EXTREME_SMALL = 0.3F;
        public static final float DEFAULT_EXTREME_LARGE = 1.7F;
        public static final int DEFAULT_MAX_IVS = 5;
        private static final float SMALL_MIN = 0.1F, SMALL_MAX = 0.9F, SMALL_STEP = 0.1F;
        private static final float LARGE_MIN = 1.1F, LARGE_MAX = 1.9F, LARGE_STEP = 0.1F;
        private static final int IVS_MIN = 3, IVS_MAX = 6, IVS_STEP = 1;
        @SerialEntry
        public float extremeSmall = DEFAULT_EXTREME_SMALL;
        @SerialEntry
        public float extremeLarge = DEFAULT_EXTREME_LARGE;
        @SerialEntry
        public int maxIvs = DEFAULT_MAX_IVS;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.group.thresholds"))
                    .option(OptionFactory.floatSlider(
                            "chiselmon.config.thresholds.extreme_small",
                            DEFAULT_EXTREME_SMALL,
                            () -> extremeSmall, v -> extremeSmall = v,
                            SMALL_MIN, SMALL_MAX, SMALL_STEP
                    ))
                    .option(OptionFactory.floatSlider(
                            "chiselmon.config.thresholds.extreme_large",
                            DEFAULT_EXTREME_LARGE,
                            () -> extremeLarge, v -> extremeLarge = v,
                            LARGE_MIN, LARGE_MAX, LARGE_STEP
                    ))
                    .option(OptionFactory.intSlider(
                            "chiselmon.config.thresholds.max_ivs",
                            DEFAULT_MAX_IVS,
                            () -> maxIvs, v -> maxIvs = v,
                            IVS_MIN, IVS_MAX, IVS_STEP
                    ))
                    .build();
        }
    }
}