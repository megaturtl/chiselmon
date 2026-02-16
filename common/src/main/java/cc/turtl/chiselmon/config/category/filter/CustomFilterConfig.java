package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.category.ConfigCategoryBuilder;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.*;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;
import static java.util.stream.Collectors.toSet;

public class CustomFilterConfig implements ConfigCategoryBuilder {

    private static final Set<String> DEFAULT_FILTER_IDS = DefaultFilters.all().stream()
            .map(f -> f.id)
            .collect(toSet());
    @SerialEntry
    public Map<String, FilterDefinition> filters = new LinkedHashMap<>();

    /**
     * Ensures default filters are present. Called after config load.
     */
    public void ensureDefaults() {
        for (FilterDefinition filter : DefaultFilters.all()) {
            filters.putIfAbsent(filter.id, filter);
        }
    }

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.filters"));

        // "Create New Filter" button at the top of custom section
        builder.option(ButtonOption.createBuilder()
                .name(modTranslatable("config.filters.create_new"))
                .description(OptionDescription.of(modTranslatable("config.filters.create_new.description")))
                .text(modTranslatable("config.filters.create_new.button"))
                .action((screen, opt) -> {
                    String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                    FilterDefinition newFilter = new FilterDefinition(
                            newId,
                            "New Custom Filter",
                            Color.WHITE,
                            Priority.NORMAL,
                            true,
                            new ArrayList<>()
                    );
                    filters.put(newId, newFilter);
                    FilterRegistry.loadFromConfig();
                    ChiselmonConfig.saveAndReloadScreen(parent);
                })
                .build());

        // Build UI for each default filter
        for (FilterDefinition filter : filters.values()) {
            builder.group(buildGroupOptions(parent, filter));

            if (!DEFAULT_FILTER_IDS.contains(filter.id)) {
                builder.group(buildTagsListGroup(filter));
            }
        }

        return builder.build();
    }

    private OptionGroup buildGroupOptions(Screen parent, FilterDefinition filter) {
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(filter.id);
        MutableComponent filterName = ComponentUtils.createComponent(filter.displayName, filter.color.getRGB());

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.group.description")))
                .collapsed(true); // Collapse by default to reduce visual clutter

        // Display name field (only for custom filters)
        if (!isDefaultFilter) {
            groupBuilder.option(OptionFactory.textField(
                    "config.filters.display_name",
                    () -> filter.displayName,
                    v -> {
                        filter.displayName = v;
                        ChiselmonConfig.saveAndReloadScreen(parent);
                        FilterRegistry.loadFromConfig();
                    }
            ));
        }

        // Color picker
        groupBuilder.option(OptionFactory.colorPicker(
                "config.filters.color",
                () -> filter.color,
                v -> {
                    filter.color = v;
                    ChiselmonConfig.saveAndReloadScreen(parent);
                    FilterRegistry.loadFromConfig();
                }
        ));

        // Priority dropdown
        groupBuilder.option(OptionFactory.enumCycler(
                "config.filters.priority",
                () -> filter.priority,
                v -> {
                    filter.priority = v;
                    FilterRegistry.loadFromConfig();
                },
                Priority.class
        ));

        // Delete button (except for default filters)
        if (!DEFAULT_FILTER_IDS.contains(filter.id)) {
            groupBuilder.option(ButtonOption.createBuilder()
                    .name(modTranslatable("config.filters.delete"))
                    .description(OptionDescription.of(modTranslatable("config.filters.delete.description")))
                    .text(modTranslatable("config.filters.delete.button"))
                    .action((screen, opt) -> {
                        filters.remove(filter.id);
                        ChiselmonConfig.saveAndReloadScreen(parent);
                        FilterRegistry.loadFromConfig();
                    })
                    .build());
        }

        return groupBuilder.build();
    }

    // New method: Add tags as a separate ListOption group
    private ListOption<String> buildTagsListGroup(FilterDefinition filter) {
        // Custom filters show "DisplayName Tags" where "Tags" is translatable
        // Format: config.filters.tags_title = "%s Tags" where %s = filter display name
        Component filterName = modTranslatable(
                "config.filters.tags_title",
                filter.displayName
        );

        return ListOption.<String>createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.tags.description")))
                .binding(
                        new ArrayList<>(filter.tags),
                        () -> new ArrayList<>(filter.tags),
                        val -> {
                            filter.tags = new ArrayList<>(val);
                            FilterRegistry.loadFromConfig();
                        }
                )
                .controller(StringControllerBuilder::create)
                .initial("")
                .collapsed(true) // Optionally collapse by default
                .build();
    }
}