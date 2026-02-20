package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.FilterDefinition;
import cc.turtl.chiselmon.api.filter.FiltersUserData;
import cc.turtl.chiselmon.api.filter.match.FilterMatcher;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.custom.HoldToConfirmButton;
import cc.turtl.chiselmon.data.UserDataRegistry;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * I am using the config for this just to provide an easy way for users to edit their own filters.
 * Filter definitions are serialized and managed by the UserDataRegistry.
 */
public class FilterConfig implements ConfigCategoryBuilder {

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        FiltersUserData filtersUserData = UserDataRegistry.get(FiltersUserData.class);

        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.filters"));

        builder.option(ButtonOption.createBuilder()
                .name(Component.translatable("chiselmon.config.filters.create").withColor(ColorUtils.GREEN.getRGB()))
                .description(OptionDescription.of(Component.translatable("chiselmon.config.filters.create.description")))
                .text(Component.translatable("chiselmon.config.filters.create.button"))
                .action((screen, opt) -> {
                    String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                    filtersUserData.put(newId, new FilterDefinition(
                            newId,
                            "New Custom Filter",
                            ColorUtils.WHITE.getRGB(),
                            Priority.NORMAL,
                            true,
                            new ArrayList<>()
                    ));
                    UserDataRegistry.save(FiltersUserData.class);
                    FilterMatcher.invalidateCache();
                    ChiselmonConfig.saveAndReloadScreen(parent, 2);
                })
                .build());

        for (FilterDefinition filter : filtersUserData.getAll().values()) {
            builder.group(buildFilterGroup(parent, filtersUserData, filter));
        }

        return builder.build();
    }

    private OptionGroup buildFilterGroup(Screen parent, FiltersUserData filtersUserData, FilterDefinition filter) {
        boolean isDefault = FilterDefinition.DefaultFilters.all().containsKey(filter.id);
        MutableComponent filterName = ComponentUtils.createComponent(filter.displayName, filter.rgb);

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(Component.translatable("chiselmon.config.filters.group.description")));

        if (!isDefault) {
            groupBuilder.option(OptionFactory.textField(
                    "chiselmon.config.filters.display_name",
                    () -> filter.displayName,
                    v -> {
                        filter.displayName = v;
                        UserDataRegistry.save(FiltersUserData.class);
                        FilterMatcher.invalidateCache();
                        ChiselmonConfig.saveAndReloadScreen(parent, 2);
                    }
            ));
        }

        groupBuilder.option(OptionFactory.colorPicker(
                "chiselmon.config.filters.color",
                () -> new Color(filter.rgb),
                v -> {
                    filter.rgb = v.getRGB();
                    UserDataRegistry.save(FiltersUserData.class);
                    FilterMatcher.invalidateCache();
                    ChiselmonConfig.saveAndReloadScreen(parent, 2);
                }
        ));

        groupBuilder.option(OptionFactory.enumCycler(
                "chiselmon.config.filters.priority",
                () -> filter.priority,
                v -> {
                    filter.priority = v;
                    UserDataRegistry.save(FiltersUserData.class);
                    FilterMatcher.invalidateCache();
                },
                Priority.class
        ));

        if (!isDefault) {
            groupBuilder.option(Option.<String>createBuilder()
                    .name(Component.translatable("chiselmon.config.filters.tags"))
                    .description(OptionDescription.createBuilder()
                            .text(Component.translatable("chiselmon.config.filters.tags.description"))
                            .text(Component.empty())
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.header")
                                    .withStyle(style -> style.withUnderlined(true)))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.shiny"))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.legendary"))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.species"))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.type"))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.gender"))
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.size"))
                            .text(Component.empty())
                            .text(Component.translatable("chiselmon.config.filters.tags.examples.usage")
                                    .withStyle(style -> style.withItalic(true)
                                            .withColor(ColorUtils.LIGHT_GRAY.getRGB())))
                            .build())
                    .binding(String.join(", ", filter.tags), () -> String.join(", ", filter.tags),
                            v -> {
                                filter.tags = Arrays.stream(v.split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList());
                                UserDataRegistry.save(FiltersUserData.class);
                                FilterMatcher.invalidateCache();
                            })
                    .controller(StringControllerBuilder::create)
                    .build());

            groupBuilder.option(HoldToConfirmButton.builder()
                    .name(Component.translatable("chiselmon.config.filters.delete", filter.displayName).withColor(ColorUtils.RED.getRGB()))
                    .description(OptionDescription.of(Component.translatable(("chiselmon.config.filters.delete.description"))))
                    .buttonText(Component.translatable("chiselmon.config.filters.delete.button"))
                    .holdingText(Component.translatable("chiselmon.config.filters.delete.held"))
                    .holdTimeTicks(30)
                    .action((screen, opt) -> {
                        filtersUserData.remove(filter.id);
                        UserDataRegistry.save(FiltersUserData.class);
                        FilterMatcher.invalidateCache();
                        ChiselmonConfig.saveAndReloadScreen(parent, 2);
                    })
                    .build());
        }

        if (isDefault) groupBuilder.collapsed(true);

        return groupBuilder.build();
    }
}