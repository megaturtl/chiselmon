package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.category.ConfigCategoryBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.*;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;
import static java.util.stream.Collectors.toSet;

public class CustomFilterConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public Map<String, FilterDefinition> filters = new LinkedHashMap<>();
    
    // Cache of default filter IDs for O(1) lookup
    private static final Set<String> DEFAULT_FILTER_IDS = DefaultFilters.all().stream()
            .map(f -> f.id)
            .collect(toSet());

    /**
     * Ensures default filters are present. Called after config load.
     */
    public void ensureDefaults() {
        for (FilterDefinition def : DefaultFilters.all()) {
            filters.putIfAbsent(def.id, def);
        }
    }

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.filters"));

        // Build UI for each filter with delete button
        for (FilterDefinition def : filters.values()) {
            builder.group(buildGroupOptions(parent, def));
            // Only show tags for custom filters (not default filters)
            if (!DEFAULT_FILTER_IDS.contains(def.id)) {
                builder.group(buildTagsListGroup(def));
            }
        }

        // Add a button to create new filters
        builder.group(OptionGroup.createBuilder()
                .name(modTranslatable("config.filters.manage"))
                .collapsed(false)  // Keep expanded for easy access
                .option(ButtonOption.createBuilder()
                        .name(modTranslatable("config.filters.add_new"))
                        .description(OptionDescription.of(modTranslatable("config.filters.add_new.description")))
                        .text(modTranslatable("config.filters.add_new.button"))
                        .action((screen, opt) -> {
                            String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                            FilterDefinition newFilter = new FilterDefinition(
                                    newId,
                                    "New Filter",
                                    Color.WHITE,
                                    Priority.NORMAL,
                                    true,
                                    new ArrayList<>()
                            );
                            filters.put(newId, newFilter);
                            FilterRegistry.loadFromConfig();
                            ChiselmonConfig.save();
                            // Stay on the filters category (index 2)
                            screen.onClose();
                            Minecraft.getInstance().setScreen(ChiselmonConfig.createScreenAtCategory(parent, 2));
                        })
                        .build())
                .build());

        return builder.build();
    }

    private OptionGroup buildGroupOptions(Screen parent, FilterDefinition def) {
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(def.id);
        Component filterName = isDefaultFilter
                ? modTranslatable("config.filters." + def.id)
                : Component.literal(def.displayName);

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.group.description")))
                .collapsed(true);  // Collapse by default to reduce visual clutter

        // Display name field (only for custom filters)
        if (!isDefaultFilter) {
            groupBuilder.option(OptionFactory.textField(
                    "config.filters.display_name",
                    () -> def.displayName,
                    v -> {
                        def.displayName = v;
                        FilterRegistry.loadFromConfig();
                    }
            ));
        }

        // Color picker
        groupBuilder.option(OptionFactory.colorPicker(
                "config.filters.color",
                () -> def.color,
                v -> {
                    def.color = v;
                    FilterRegistry.loadFromConfig();
                }
        ));

        // Priority dropdown
        groupBuilder.option(OptionFactory.enumDropdown(
                "config.filters.priority",
                () -> def.priority,
                v -> {
                    def.priority = v;
                    FilterRegistry.loadFromConfig();
                },
                Priority.class
        ));

        // Delete button (except for default filters)
        if (!DEFAULT_FILTER_IDS.contains(def.id)) {
            groupBuilder.option(ButtonOption.createBuilder()
                    .name(modTranslatable("config.filters.delete"))
                    .description(OptionDescription.of(modTranslatable("config.filters.delete.description")))
                    .text(modTranslatable("config.filters.delete.button"))
                    .action((screen, opt) -> {
                        filters.remove(def.id);
                        FilterRegistry.loadFromConfig();
                        ChiselmonConfig.save();
                        // Stay on the filters category (index 2)
                        screen.onClose();
                        Minecraft.getInstance().setScreen(ChiselmonConfig.createScreenAtCategory(parent, 2));
                    })
                    .build());
        }

        return groupBuilder.build();
    }

    // New method: Add tags as a separate ListOption group
    private ListOption<String> buildTagsListGroup(FilterDefinition def) {
        // Custom filters show "DisplayName Tags" where "Tags" is translatable
        // Format: config.filters.tags_title = "%s Tags" where %s = filter display name
        Component filterName = Component.translatable(
                "config.filters.tags_title",
                def.displayName
        );

        return ListOption.<String>createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.tags.description")))
                .binding(
                        new ArrayList<>(def.tags),
                        () -> new ArrayList<>(def.tags),
                        val -> {
                            def.tags = new ArrayList<>(val);
                            FilterRegistry.loadFromConfig();
                        }
                )
                .controller(StringControllerBuilder::create)
                .initial("")
                .collapsed(true) // Optionally collapse by default
                .build();
    }
}