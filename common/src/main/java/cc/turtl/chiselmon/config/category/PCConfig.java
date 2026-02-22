package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.feature.pc.sort.SortMode;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PCConfig {

    @SerialEntry
    public final QuickSortConfig quickSort = new QuickSortConfig();
    @SerialEntry
    public final TooltipConfig tooltip = new TooltipConfig();
    @SerialEntry
    public final IconConfig icon = new IconConfig();
    @SerialEntry
    public final EggSpyConfig eggSpy = new EggSpyConfig();

    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.pc"))
                .group(quickSort.buildGroup())
                .group(tooltip.buildGroup())
                .group(icon.buildGroup())
                .group(eggSpy.buildGroup())
                .build();
    }

    public static class QuickSortConfig {
        public static final boolean DEFAULT_ENABLED = false;
        public static final SortMode DEFAULT_MODE = SortMode.POKEDEX_NUMBER;
        public static final InputConstants.Key DEFAULT_HOTKEY = InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);

        @SerialEntry
        public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry
        public SortMode mode = DEFAULT_MODE;
        @SerialEntry
        public InputConstants.Key hotkey = DEFAULT_HOTKEY;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.pc.group.quick_sort"))
                    .option(OptionFactory.toggleOnOff(
                            "chiselmon.config.pc.quick_sort.enabled",
                            DEFAULT_ENABLED,
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.enumCycler(
                            "chiselmon.config.pc.quick_sort.mode",
                            DEFAULT_MODE,
                            () -> mode,
                            v -> mode = v,
                            SortMode.class
                    ))
                    .option(OptionFactory.hotkeyPicker(
                            "chiselmon.config.pc.quick_sort.hotkey",
                            DEFAULT_HOTKEY,
                            () -> hotkey,
                            v -> hotkey = v
                    ))
                    .build();
        }
    }

    public static class EggSpyConfig {
        public static final boolean DEFAULT_ENABLED = false;
        public static final boolean DEFAULT_SYNC_HATCH_PROGRESS = false;

        @SerialEntry
        public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry
        public boolean syncHatchProgress = DEFAULT_SYNC_HATCH_PROGRESS;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.pc.group.egg_spy"))
                    .option(OptionFactory.toggleOnOff(
                            "chiselmon.config.pc.egg_spy.enabled",
                            DEFAULT_ENABLED,
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.sync_hatch_progress.enabled",
                            DEFAULT_SYNC_HATCH_PROGRESS,
                            () -> syncHatchProgress,
                            v -> syncHatchProgress = v
                    ))
                    .build();
        }
    }

    public static class TooltipConfig {
        public static final boolean DEFAULT_ENABLED = true;
        public static final boolean DEFAULT_SHOW_ON_HOVER = false;
        public static final boolean DEFAULT_EXTEND_ON_SHIFT = true;
        public static final boolean DEFAULT_IVS = true;
        public static final boolean DEFAULT_ORIGINAL_TRAINER = true;
        public static final boolean DEFAULT_FORM = true;
        public static final boolean DEFAULT_FRIENDSHIP = false;
        public static final boolean DEFAULT_RIDE_STYLES = true;
        public static final boolean DEFAULT_MARKS = true;
        public static final boolean DEFAULT_HATCH_PROGRESS = false;

        @SerialEntry public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry public boolean showOnHover = DEFAULT_SHOW_ON_HOVER;
        @SerialEntry public boolean extendOnShift = DEFAULT_EXTEND_ON_SHIFT;
        @SerialEntry public boolean ivs = DEFAULT_IVS;
        @SerialEntry public boolean originalTrainer = DEFAULT_ORIGINAL_TRAINER;
        @SerialEntry public boolean form = DEFAULT_FORM;
        @SerialEntry public boolean friendship = DEFAULT_FRIENDSHIP;
        @SerialEntry public boolean rideStyles = DEFAULT_RIDE_STYLES;
        @SerialEntry public boolean marks = DEFAULT_MARKS;
        @SerialEntry public boolean hatchProgress = DEFAULT_HATCH_PROGRESS;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.group.tooltip"))
                    .option(OptionFactory.toggleOnOff(
                            "chiselmon.config.pc.tooltip.enabled",
                            DEFAULT_ENABLED,
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.show_on_hover",
                            DEFAULT_SHOW_ON_HOVER,
                            () -> showOnHover,
                            v -> showOnHover = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.extend_on_shift",
                            DEFAULT_EXTEND_ON_SHIFT,
                            () -> extendOnShift,
                            v -> extendOnShift = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.ivs",
                            DEFAULT_IVS,
                            () -> ivs,
                            v -> ivs = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.original_trainer",
                            DEFAULT_ORIGINAL_TRAINER,
                            () -> originalTrainer,
                            v -> originalTrainer = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.form",
                            DEFAULT_FORM,
                            () -> form,
                            v -> form = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.friendship",
                            DEFAULT_FRIENDSHIP,
                            () -> friendship,
                            v -> friendship = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.ride_styles",
                            DEFAULT_RIDE_STYLES,
                            () -> rideStyles,
                            v -> rideStyles = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.marks",
                            DEFAULT_MARKS,
                            () -> marks,
                            v -> marks = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.tooltip.hatch_progress",
                            DEFAULT_HATCH_PROGRESS,
                            () -> hatchProgress,
                            v -> hatchProgress = v
                    ))
                    .build();
        }
    }

    public static class IconConfig {
        public static final boolean DEFAULT_ENABLED = true;
        public static final boolean DEFAULT_HIDDEN_ABILITY = true;
        public static final boolean DEFAULT_IVS = true;
        public static final boolean DEFAULT_SHINY = true;
        public static final boolean DEFAULT_SIZE = true;
        public static final boolean DEFAULT_MARK = true;
        public static final boolean DEFAULT_RIDEABLE = false;

        @SerialEntry public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry public boolean hiddenAbility = DEFAULT_HIDDEN_ABILITY;
        @SerialEntry public boolean ivs = DEFAULT_IVS;
        @SerialEntry public boolean shiny = DEFAULT_SHINY;
        @SerialEntry public boolean size = DEFAULT_SIZE;
        @SerialEntry public boolean mark = DEFAULT_MARK;
        @SerialEntry public boolean rideable = DEFAULT_RIDEABLE;

        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(Component.translatable("chiselmon.config.group.icon"))
                    .option(OptionFactory.toggleOnOff(
                            "chiselmon.config.pc.icon.enabled",
                            DEFAULT_ENABLED,
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.hidden_ability",
                            DEFAULT_HIDDEN_ABILITY,
                            () -> hiddenAbility,
                            v -> hiddenAbility = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.ivs",
                            DEFAULT_IVS,
                            () -> ivs,
                            v -> ivs = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.shiny",
                            DEFAULT_SHINY,
                            () -> shiny,
                            v -> shiny = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.size",
                            DEFAULT_SIZE,
                            () -> size,
                            v -> size = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.mark",
                            DEFAULT_MARK,
                            () -> mark,
                            v -> mark = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "chiselmon.config.pc.icon.rideable",
                            DEFAULT_RIDEABLE,
                            () -> rideable,
                            v -> rideable = v
                    ))
                    .build();
        }
    }
}