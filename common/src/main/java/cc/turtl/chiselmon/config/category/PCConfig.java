package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.feature.pc.sort.SortMode;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;

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
        @SerialEntry(comment = "Enable quick-sort on middle-click")
        public boolean enabled = false;

        @SerialEntry(comment = "Sorting method to use")
        public SortMode mode = SortMode.POKEDEX_NUMBER;

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
        @SerialEntry(comment = "Show extra details of pokemon when hovered")
        public boolean enabled = true;

        @SerialEntry(comment = "Show a one-line tooltip when hovering over a pokemon")
        public boolean showOnHover = false;

        @SerialEntry(comment = "Show a multi-line tooltip when holding shift")
        public boolean extendOnShift = true;

        @SerialEntry(comment = "Display IVs")
        public boolean ivs = true;

        @SerialEntry(comment = "Display original trainer username")
        public boolean originalTrainer = true;

        @SerialEntry(comment = "Display pokemon form")
        public boolean form = true;

        @SerialEntry(comment = "Display friendship level")
        public boolean friendship = false;

        @SerialEntry(comment = "Display available ride styles")
        public boolean rideStyles = true;

        @SerialEntry(comment = "Display pokemon marks")
        public boolean marks = true;

        @SerialEntry(comment = "Display egg hatch progress")
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
        @SerialEntry(comment = "Identify special pokemon at a glance with icons")
        public boolean enabled = true;

        @SerialEntry(comment = "Show an ability patch icon for hidden ability pokemon")
        public boolean hiddenAbility = true;

        @SerialEntry(comment = "Show a gold bottle cap icon for pokemon with high IVs (defined in thresholds)")
        public boolean ivs = true;

        @SerialEntry(comment = "Show a pink sparkle icon for shiny pokemon")
        public boolean shiny = true;

        @SerialEntry(comment = "Show a green mushroom icon for extreme size pokemon (defined in thresholds)")
        public boolean size = true;

        @SerialEntry(comment = "Show a purple trophy icon for marked pokemon")
        public boolean mark = true;

        @SerialEntry(comment = "Show a brown saddle icon for rideable pokemon")
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
