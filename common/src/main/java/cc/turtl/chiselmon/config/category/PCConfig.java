package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.feature.pc.sort.SortMode;
import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class PCConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public final QuickSortConfig quickSort = new QuickSortConfig();
    @SerialEntry
    public final TooltipConfig tooltip = new TooltipConfig();
    @SerialEntry
    public final IconConfig icon = new IconConfig();
    @SerialEntry
    public final EggSpyConfig eggSpy = new EggSpyConfig();

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.pc"))
                .group(quickSort.buildGroup())
                .group(tooltip.buildGroup())
                .group(icon.buildGroup())
                .group(eggSpy.buildGroup())
                .build();
    }

    public static class QuickSortConfig implements ConfigGroupBuilder {
        @SerialEntry
        public boolean enabled = false;

        @SerialEntry
        public SortMode mode = SortMode.POKEDEX_NUMBER;

        @SerialEntry
        public InputConstants.Key hotkey = InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(modTranslatable("config.pc.group.quick_sort"))
                    .option(OptionFactory.toggleOnOff(
                            "config.pc.quick_sort.enabled",
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.enumCycler(
                            "config.pc.quick_sort.mode",
                            () -> mode,
                            v -> mode = v,
                            SortMode.class
                    ))
                    .option(OptionFactory.hotkeyPicker(
                            "config.pc.quick_sort.hotkey",
                            () -> hotkey,
                            v -> hotkey = v
                    ))
                    .build();
        }
    }

    public static class EggSpyConfig implements ConfigGroupBuilder {
        @SerialEntry(comment = "Show a preview of what's inside eggs")
        public boolean enabled = false;

        @SerialEntry(comment = "EXPERIMENTAL: Attempt to auto sync hatch progress without manually putting in/out of PC")
        public boolean syncHatchProgress = false;

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(modTranslatable("config.pc.group.egg_spy"))
                    .option(OptionFactory.toggleOnOff(
                            "config.pc.egg_spy.enabled",
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.sync_hatch_progress.enabled",
                            () -> syncHatchProgress,
                            v -> syncHatchProgress = v
                    ))
                    .build();
        }
    }

    public static class TooltipConfig implements ConfigGroupBuilder {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean showOnHover = false;

        @SerialEntry
        public boolean extendOnShift = true;

        @SerialEntry
        public boolean ivs = true;

        @SerialEntry
        public boolean originalTrainer = true;

        @SerialEntry
        public boolean form = true;

        @SerialEntry
        public boolean friendship = false;

        @SerialEntry
        public boolean rideStyles = true;

        @SerialEntry
        public boolean marks = true;

        @SerialEntry
        public boolean hatchProgress = false;

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(modTranslatable("config.group.tooltip"))
                    .option(OptionFactory.toggleOnOff(
                            "config.pc.tooltip.enabled",
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.show_on_hover",
                            () -> showOnHover,
                            v -> showOnHover = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.extend_on_shift",
                            () -> extendOnShift,
                            v -> extendOnShift = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.ivs",
                            () -> ivs,
                            v -> ivs = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.original_trainer",
                            () -> originalTrainer,
                            v -> originalTrainer = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.form",
                            () -> form,
                            v -> form = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.friendship",
                            () -> friendship,
                            v -> friendship = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.ride_styles",
                            () -> rideStyles,
                            v -> rideStyles = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.marks",
                            () -> marks,
                            v -> marks = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.tooltip.hatch_progress",
                            () -> hatchProgress,
                            v -> hatchProgress = v
                    ))
                    .build();
        }
    }

    public static class IconConfig implements ConfigGroupBuilder {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean hiddenAbility = true;

        @SerialEntry
        public boolean ivs = true;

        @SerialEntry
        public boolean shiny = true;

        @SerialEntry
        public boolean size = true;

        @SerialEntry
        public boolean mark = true;

        @SerialEntry
        public boolean rideable = false;

        @Override
        public OptionGroup buildGroup() {
            return OptionGroup.createBuilder()
                    .name(modTranslatable("config.group.icon"))
                    .option(OptionFactory.toggleOnOff(
                            "config.pc.icon.enabled",
                            () -> enabled,
                            v -> enabled = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.hidden_ability",
                            () -> hiddenAbility,
                            v -> hiddenAbility = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.ivs",
                            () -> ivs,
                            v -> ivs = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.shiny",
                            () -> shiny,
                            v -> shiny = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.size",
                            () -> size,
                            v -> size = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.mark",
                            () -> mark,
                            v -> mark = v
                    ))
                    .option(OptionFactory.toggleTick(
                            "config.pc.icon.rideable",
                            () -> rideable,
                            v -> rideable = v
                    ))
                    .build();
        }
    }
}
