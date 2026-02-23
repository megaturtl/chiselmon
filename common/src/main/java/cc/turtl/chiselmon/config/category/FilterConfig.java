package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonStorage;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.*;
import cc.turtl.chiselmon.api.filter.match.FilterMatcher;
import cc.turtl.chiselmon.api.storage.StorageScope;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.custom.HoldToConfirmButton;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * I am using the config for this just to provide an easy way for users to edit their own filters.
 * Filter definitions are serialized and managed by the UserDataRegistry.
 */
public class FilterConfig {

    public static Color DEFAULT_COLOR = ColorUtils.WHITE;
    public static Priority DEFAULT_PRIORITY = Priority.NORMAL;
    public static String DEFAULT_DISPLAY_NAME = "New Custom Filter";
    public static String DEFAULT_CONDITION_STRING = "shiny";

    public ConfigCategory buildCategory(Screen parent) {
        FiltersUserData filtersUserData = ChiselmonStorage.FILTERS.get(StorageScope.global());

        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.filters"));

        builder.option(ButtonOption.createBuilder()
                .name(Component.translatable("chiselmon.config.filters.create")
                        .withColor(ColorUtils.GREEN.getRGB()))
                .description(OptionDescription.of(
                        Component.translatable("chiselmon.config.filters.create.description")))
                .text(Component.translatable("chiselmon.config.filters.create.button"))
                .action((screen, opt) -> {
                    String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                    filtersUserData.put(newId, new FilterDefinition(
                            newId,
                            DEFAULT_DISPLAY_NAME,
                            DEFAULT_COLOR.getRGB(),
                            DEFAULT_PRIORITY,
                            DEFAULT_CONDITION_STRING
                    ));
                    saveAndReload(parent);
                })
                .build());

        for (FilterDefinition filter : filtersUserData.getAll().values()) {
            builder.group(buildFilterGroup(parent, filtersUserData, filter));
        }

        return builder.build();
    }

    private OptionGroup buildFilterGroup(Screen parent, FiltersUserData filtersUserData,
                                         FilterDefinition filter) {
        boolean isDefault = FilterDefinition.DefaultFilters.all().containsKey(filter.id);
        MutableComponent filterName = ComponentUtils.createComponent(filter.displayName, filter.rgb);

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(
                        Component.translatable("chiselmon.config.filters.group.description")));

        if (!isDefault) {
            groupBuilder.option(OptionFactory.textField(
                    "chiselmon.config.filters.display_name",
                    filter.displayName,
                    () -> filter.displayName,
                    v -> {
                        filter.displayName = v;
                        saveAndReload(parent);
                    }
            ));
        }

        groupBuilder.option(OptionFactory.colorPicker(
                "chiselmon.config.filters.color",
                new Color(filter.rgb),
                () -> new Color(filter.rgb),
                v -> {
                    filter.rgb = v.getRGB();
                    saveAndReload(parent);
                }
        ));

        groupBuilder.option(OptionFactory.enumCycler(
                "chiselmon.config.filters.priority",
                filter.priority,
                () -> filter.priority,
                v -> {
                    filter.priority = v;
                    ChiselmonStorage.FILTERS.save(StorageScope.global());
                    FilterMatcher.invalidateCache();
                },
                Priority.class
        ));

        if (!isDefault) {
            groupBuilder.option(Option.<String>createBuilder()
                    .name(Component.translatable("chiselmon.config.filters.condition"))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("chiselmon.config.filters.condition.description"))
                            .text(Component.empty())
                            .text(Component.translatable("chiselmon.config.filters.condition.syntax.header")
                                    .withStyle(s -> s.withUnderlined(true)))
                            .text(Component.literal("◆ shiny AND type=fire"))
                            .text(Component.literal("◆ legendary OR shiny"))
                            .text(Component.literal("◆ NOT species=skitty AND min_size=1.5"))
                            .text(Component.literal("◆ (shiny OR legendary) AND NOT species=magikarp"))
                            .text(Component.empty())
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.header")
                                    .withStyle(s -> s.withUnderlined(true)))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.shiny"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.legendary"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.species"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.type"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.gender"))
                            .text(Component.translatable("chiselmon.config.filters.condition.tags.examples.size"))
                            .build())
                    .binding(DEFAULT_CONDITION_STRING, () -> filter.conditionString, v -> {
                        filter.conditionString = v.trim();
                        ChiselmonStorage.FILTERS.save(StorageScope.global());
                        FilterMatcher.invalidateCache();
                    })
                    .controller(StringControllerBuilder::create)
                    .build());

            groupBuilder.option(HoldToConfirmButton.builder()
                    .name(Component.translatable("chiselmon.config.filters.delete", filter.displayName)
                            .withColor(ColorUtils.RED.getRGB()))
                    .description(OptionDescription.of(
                            Component.translatable("chiselmon.config.filters.delete.description")))
                    .buttonText(Component.translatable("chiselmon.config.filters.delete.button"))
                    .holdingText(Component.translatable("chiselmon.config.filters.delete.held"))
                    .holdTimeTicks(30)
                    .action((screen, opt) -> {
                        filtersUserData.remove(filter.id);
                        saveAndReload(parent);
                    })
                    .build());
        }

        if (isDefault) groupBuilder.collapsed(true);
        return groupBuilder.build();
    }

    private void saveAndReload(Screen parent) {
        ChiselmonStorage.FILTERS.save(StorageScope.global());
        FilterMatcher.invalidateCache();
        ChiselmonConfig.saveAndReloadScreen(parent, 2);
    }
}