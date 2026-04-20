package cc.turtl.chiselmon.client.config.category

import cc.turtl.chiselmon.client.ChiselmonKeybinds
import cc.turtl.turtlshell.api.client.config.OptionFactory
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.network.chat.Component

class GeneralConfig {

    @SerialEntry
    var modDisabled: Boolean = DEFAULT_MOD_DISABLED

    @SerialEntry
    var moveDetail: Boolean = DEFAULT_MOVE_DETAIL

    @SerialEntry
    var checkSpawnDetail: Boolean = DEFAULT_CHECKSPAWN_DETAIL

    @SerialEntry
    var discordWebhookURL: String = ""

    @SerialEntry
    val eggSpy: EggSpyConfig = EggSpyConfig()

    @SerialEntry
    val thresholds: ThresholdsConfig = ThresholdsConfig()

    fun buildCategory(): ConfigCategory = ConfigCategory.createBuilder()
        .name(Component.translatable("chiselmon.config.category.general"))
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.general.mod_disabled",
                DEFAULT_MOD_DISABLED,
                { modDisabled },
                { modDisabled = it })
        )
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.general.move_detail",
                DEFAULT_MOVE_DETAIL,
                { moveDetail },
                { moveDetail = it })
        )
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.general.checkspawn_detail",
                DEFAULT_CHECKSPAWN_DETAIL,
                { checkSpawnDetail },
                { checkSpawnDetail = it })
        )
        .option(
            OptionFactory.textField(
                "chiselmon.config.general.discord_webhook_url",
                "",
                { discordWebhookURL },
                { discordWebhookURL = it })
        )
        .option(
            OptionFactory.keyMappingPicker(
                "chiselmon.config.general.open_config_keybind",
                ChiselmonKeybinds.OPEN_CONFIG
            )
        )
        .group(eggSpy.buildGroup())
        .group(thresholds.buildGroup())
        .build()

    companion object {
        const val DEFAULT_MOD_DISABLED = false
        const val DEFAULT_MOVE_DETAIL = true
        const val DEFAULT_CHECKSPAWN_DETAIL = true
    }

    class EggSpyConfig {

        @SerialEntry
        var enabled: Boolean = DEFAULT_ENABLED

        @SerialEntry
        var showHatchOverlay: Boolean = DEFAULT_SHOW_HATCH_OVERLAY

        fun buildGroup(): OptionGroup = OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.group.egg_spy"))
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.egg_spy.enabled",
                    DEFAULT_ENABLED,
                    { enabled },
                    { enabled = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.egg_spy.hatch_overlay",
                    DEFAULT_SHOW_HATCH_OVERLAY,
                    { showHatchOverlay },
                    { showHatchOverlay = it })
            )
            .build()

        companion object {
            const val DEFAULT_ENABLED = false
            const val DEFAULT_SHOW_HATCH_OVERLAY = false
        }
    }

    class ThresholdsConfig {

        @SerialEntry
        var extremeSmall: Float = DEFAULT_EXTREME_SMALL

        @SerialEntry
        var extremeLarge: Float = DEFAULT_EXTREME_LARGE

        @SerialEntry
        var maxIvs: Int = DEFAULT_MAX_IVS

        fun buildGroup(): OptionGroup = OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.group.thresholds"))
            .option(
                OptionFactory.floatSlider(
                    "chiselmon.config.thresholds.extreme_small",
                    DEFAULT_EXTREME_SMALL,
                    { extremeSmall }, { extremeSmall = it },
                    SMALL_MIN, SMALL_MAX, SMALL_STEP
                )
            )
            .option(
                OptionFactory.floatSlider(
                    "chiselmon.config.thresholds.extreme_large",
                    DEFAULT_EXTREME_LARGE,
                    { extremeLarge }, { extremeLarge = it },
                    LARGE_MIN, LARGE_MAX, LARGE_STEP
                )
            )
            .option(
                OptionFactory.intSlider(
                    "chiselmon.config.thresholds.max_ivs",
                    DEFAULT_MAX_IVS,
                    { maxIvs }, { maxIvs = it },
                    IVS_MIN, IVS_MAX, IVS_STEP
                )
            )
            .build()

        companion object {
            const val DEFAULT_EXTREME_SMALL = 0.3F
            const val DEFAULT_EXTREME_LARGE = 1.7F
            const val DEFAULT_MAX_IVS = 5

            const val SMALL_MIN = 0.1F
            const val SMALL_MAX = 0.9F
            const val SMALL_STEP = 0.1F

            const val LARGE_MIN = 1.1F
            const val LARGE_MAX = 1.9F
            const val LARGE_STEP = 0.1F

            const val IVS_MIN = 3
            const val IVS_MAX = 6
            const val IVS_STEP = 1
        }
    }
}