package cc.turtl.chiselmon.client.config.category

import cc.turtl.chiselmon.client.ChiselmonPacks
import cc.turtl.chiselmon.client.feature.pc.sort.SortMode
import cc.turtl.turtlshell.api.client.config.OptionFactory
import com.mojang.blaze3d.platform.InputConstants
import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.Util
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

class PCConfig {

    @SerialEntry
    val quickSort: QuickSortConfig = QuickSortConfig()

    @SerialEntry
    val tooltip: TooltipConfig = TooltipConfig()

    @SerialEntry
    val icon: IconConfig = IconConfig()

    fun buildCategory(): ConfigCategory = ConfigCategory.createBuilder()
        .name(Component.translatable("chiselmon.config.category.pc"))
        .option(
            ButtonOption.createBuilder()
                .name(Component.translatable("chiselmon.config.pc.open_wallpaper_folder"))
                .text(Component.translatable("chiselmon.config.pc.open_wallpaper_folder.text"))
                .description(OptionDescription.of(Component.translatable("chiselmon.config.pc.open_wallpaper_folder.description")))
                .action { _, _ ->
                    val userWallpaperPath = ChiselmonPacks.getOrCreateCustomWallpaperDir()
                    Util.getPlatform().openPath(userWallpaperPath)
                }
                .build()
        )
        .group(quickSort.buildGroup())
        .group(tooltip.buildGroup())
        .group(icon.buildGroup())
        .build()

    class QuickSortConfig {

        @SerialEntry
        var enabled: Boolean = DEFAULT_ENABLED

        @SerialEntry
        var mode: SortMode = DEFAULT_MODE

        @SerialEntry
        var hotkey: InputConstants.Key = DEFAULT_HOTKEY

        fun buildGroup(): OptionGroup = OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.pc.group.quick_sort"))
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.pc.quick_sort.enabled",
                    DEFAULT_ENABLED,
                    { enabled },
                    { enabled = it })
            )
            .option(
                OptionFactory.enumCycler(
                    "chiselmon.config.pc.quick_sort.mode",
                    DEFAULT_MODE,
                    { mode },
                    { mode = it },
                    SortMode::class.java
                )
            )
            .option(
                OptionFactory.hotkeyPicker(
                    "chiselmon.config.pc.quick_sort.hotkey",
                    DEFAULT_HOTKEY,
                    { hotkey },
                    { hotkey = it })
            )
            .build()

        companion object {
            const val DEFAULT_ENABLED = false
            val DEFAULT_MODE = SortMode.POKEDEX_NUMBER
            val DEFAULT_HOTKEY: InputConstants.Key =
                InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        }
    }

    class TooltipConfig {

        @SerialEntry
        var enabled: Boolean = DEFAULT_ENABLED

        @SerialEntry
        var showOnHover: Boolean = DEFAULT_SHOW_ON_HOVER

        @SerialEntry
        var extendOnShift: Boolean = DEFAULT_EXTEND_ON_SHIFT

        @SerialEntry
        var ivs: Boolean = DEFAULT_IVS

        @SerialEntry
        var originalTrainer: Boolean = DEFAULT_ORIGINAL_TRAINER

        @SerialEntry
        var form: Boolean = DEFAULT_FORM

        @SerialEntry
        var friendship: Boolean = DEFAULT_FRIENDSHIP

        @SerialEntry
        var rideStyles: Boolean = DEFAULT_RIDE_STYLES

        @SerialEntry
        var marks: Boolean = DEFAULT_MARKS

        @SerialEntry
        var hatchProgress: Boolean = DEFAULT_HATCH_PROGRESS

        fun buildGroup(): OptionGroup = OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.pc.group.tooltip"))
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.pc.tooltip.enabled",
                    DEFAULT_ENABLED,
                    { enabled },
                    { enabled = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.show_on_hover",
                    DEFAULT_SHOW_ON_HOVER,
                    { showOnHover },
                    { showOnHover = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.extend_on_shift",
                    DEFAULT_EXTEND_ON_SHIFT,
                    { extendOnShift },
                    { extendOnShift = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.ivs",
                    DEFAULT_IVS,
                    { ivs },
                    { ivs = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.original_trainer",
                    DEFAULT_ORIGINAL_TRAINER,
                    { originalTrainer },
                    { originalTrainer = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.form",
                    DEFAULT_FORM,
                    { form },
                    { form = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.friendship",
                    DEFAULT_FRIENDSHIP,
                    { friendship },
                    { friendship = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.ride_styles",
                    DEFAULT_RIDE_STYLES,
                    { rideStyles },
                    { rideStyles = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.marks",
                    DEFAULT_MARKS,
                    { marks },
                    { marks = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.tooltip.hatch_progress",
                    DEFAULT_HATCH_PROGRESS,
                    { hatchProgress },
                    { hatchProgress = it })
            )
            .build()

        companion object {
            const val DEFAULT_ENABLED = true
            const val DEFAULT_SHOW_ON_HOVER = false
            const val DEFAULT_EXTEND_ON_SHIFT = true
            const val DEFAULT_IVS = true
            const val DEFAULT_ORIGINAL_TRAINER = true
            const val DEFAULT_FORM = true
            const val DEFAULT_FRIENDSHIP = false
            const val DEFAULT_RIDE_STYLES = false
            const val DEFAULT_MARKS = true
            const val DEFAULT_HATCH_PROGRESS = false
        }
    }

    class IconConfig {

        @SerialEntry
        var enabled: Boolean = DEFAULT_ENABLED

        @SerialEntry
        var hiddenAbility: Boolean = DEFAULT_HIDDEN_ABILITY

        @SerialEntry
        var ivs: Boolean = DEFAULT_IVS

        @SerialEntry
        var shiny: Boolean = DEFAULT_SHINY

        @SerialEntry
        var size: Boolean = DEFAULT_SIZE

        @SerialEntry
        var mark: Boolean = DEFAULT_MARK

        @SerialEntry
        var rideable: Boolean = DEFAULT_RIDEABLE

        @SerialEntry
        var shoulderable: Boolean = DEFAULT_SHOULDERABLE

        fun buildGroup(): OptionGroup = OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.pc.group.icon"))
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.pc.icon.enabled",
                    DEFAULT_ENABLED,
                    { enabled },
                    { enabled = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.hidden_ability",
                    DEFAULT_HIDDEN_ABILITY,
                    { hiddenAbility },
                    { hiddenAbility = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.ivs",
                    DEFAULT_IVS,
                    { ivs },
                    { ivs = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.shiny",
                    DEFAULT_SHINY,
                    { shiny },
                    { shiny = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.size",
                    DEFAULT_SIZE,
                    { size },
                    { size = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.mark",
                    DEFAULT_MARK,
                    { mark },
                    { mark = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.rideable",
                    DEFAULT_RIDEABLE,
                    { rideable },
                    { rideable = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.pc.icon.shoulderable",
                    DEFAULT_SHOULDERABLE,
                    { shoulderable },
                    { shoulderable = it })
            )
            .build()

        companion object {
            const val DEFAULT_ENABLED = true
            const val DEFAULT_HIDDEN_ABILITY = true
            const val DEFAULT_IVS = true
            const val DEFAULT_SHINY = true
            const val DEFAULT_SIZE = true
            const val DEFAULT_MARK = true
            const val DEFAULT_RIDEABLE = false
            const val DEFAULT_SHOULDERABLE = false
        }
    }
}