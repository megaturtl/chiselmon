package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.DefaultFilters;
import cc.turtl.chiselmon.api.filter.FilterDefinition;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.custom.HoldToConfirmButton;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class FilterConfig implements ConfigCategoryBuilder {

    private static final Set<String> DEFAULT_FILTER_IDS = DefaultFilters.all().stream()
            .map(f -> f.id)
            .collect(Collectors.toSet());

    @SerialEntry
    public Map<String, FilterDefinition> filters = new LinkedHashMap<>();

    /**
     * Ensures default filters are present and up-to-date. Called after config load.
     */
    public void ensureDefaults() {
        for (FilterDefinition defaultFilter : DefaultFilters.all()) {
            filters.put(defaultFilter.id, defaultFilter);
        }
    }

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.filters"));

        // "Create New Filter" button
        builder.option(ButtonOption.createBuilder()
                .name(modTranslatable("config.filters.create").withColor(ColorUtils.GREEN.getRGB()))
                .description(OptionDescription.of(modTranslatable("config.filters.create.description")))
                .text(modTranslatable("config.filters.create.button"))
                .action((screen, opt) -> {
                    String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                    FilterDefinition newFilter = new FilterDefinition(
                            newId,
                            "New Custom Filter",
                            ColorUtils.WHITE,
                            Priority.NORMAL,
                            true,
                            new ArrayList<>()
                    );
                    filters.put(newId, newFilter);
                    ChiselmonConfig.saveAndReloadScreen(parent, 2);
                })
                .build());

        // Build UI for each filter
        for (FilterDefinition filter : filters.values()) {
            builder.group(buildFilterGroup(parent, filter));
        }

        return builder.build();
    }

    private OptionGroup buildFilterGroup(Screen parent, FilterDefinition filter) {
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(filter.id);
        MutableComponent filterName = ComponentUtils.createComponent(filter.displayName, filter.color.getRGB());

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.group.description")));

        // Display name (custom filters only)
        if (!isDefaultFilter) {
            groupBuilder.option(OptionFactory.textField(
                    "config.filters.display_name",
                    () -> filter.displayName,
                    v -> {
                        filter.displayName = v;
                        ChiselmonConfig.saveAndReloadScreen(parent, 2);
                    }
            ));
        }

        // Color picker
        groupBuilder.option(OptionFactory.colorPicker(
                "config.filters.color",
                () -> filter.color,
                v -> {
                    filter.color = v;
                    ChiselmonConfig.saveAndReloadScreen(parent, 2);
                }
        ));

        // Priority dropdown
        groupBuilder.option(OptionFactory.enumCycler(
                "config.filters.priority",
                () -> filter.priority,
                v -> {
                    filter.priority = v;
                    ChiselmonConfig.save();
                },
                Priority.class
        ));

        // Tags (custom filters only)
        if (!isDefaultFilter) {
            groupBuilder.option(Option.<String>createBuilder()
                    .name(modTranslatable("config.filters.tags"))
                    .description(OptionDescription.createBuilder()
                            .text(modTranslatable("config.filters.tags.description"))
                            .text(Component.empty())
                            .text(modTranslatable("config.filters.tags.examples.header")
                                    .withStyle(style -> style.withUnderlined(true)))
                            .text(modTranslatable("config.filters.tags.examples.shiny"))
                            .text(modTranslatable("config.filters.tags.examples.legendary"))
                            .text(modTranslatable("config.filters.tags.examples.species"))
                            .text(modTranslatable("config.filters.tags.examples.type"))
                            .text(modTranslatable("config.filters.tags.examples.gender"))
                            .text(modTranslatable("config.filters.tags.examples.size"))
                            .text(Component.empty())
                            .text(modTranslatable("config.filters.tags.examples.usage")
                                    .withStyle(style -> style.withItalic(true)
                                            .withColor(ColorUtils.LIGHT_GRAY.getRGB())))
                            .build())
                    .binding(String.join(", ", filter.tags), () -> String.join(", ", filter.tags),
                            v -> {
                                filter.tags = Arrays.stream(v.split(","))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList());
                                ChiselmonConfig.save();
                            })
                    .controller(StringControllerBuilder::create)
                    .build());
        }

        // Delete button (custom filters only)
        if (!isDefaultFilter) {
            groupBuilder.option(HoldToConfirmButton.builder()
                    .name(modTranslatable("config.filters.delete", filter.displayName).withColor(ColorUtils.RED.getRGB()))
                    .description(OptionDescription.of(modTranslatable("config.filters.delete.description")))
                    .buttonText(modTranslatable("config.filters.delete.button"))
                    .holdingText(modTranslatable("config.filters.delete.held"))
                    .holdTimeTicks(30)
                    .action((screen, opt) -> {
                        filters.remove(filter.id);
                        ChiselmonConfig.saveAndReloadScreen(parent, 2);
                    })
                    .build());
        }

        if (isDefaultFilter) {
            groupBuilder.collapsed(true);
        }

        return groupBuilder.build();
    }
}