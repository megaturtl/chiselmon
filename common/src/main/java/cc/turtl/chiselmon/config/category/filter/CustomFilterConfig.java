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
            builder.group(buildTagsListGroup(def)); // Add tags as separate group
        }

        // Add a button to create new filters
        builder.group(OptionGroup.createBuilder()
                .name(modTranslatable("config.filters.manage"))
                .option(ButtonOption.createBuilder()
                        .name(modTranslatable("config.filters.add_new"))
                        .description(OptionDescription.of(modTranslatable("config.filters.add_new.description")))
                        .action((screen, opt) -> {
                            String newId = "custom_" + UUID.randomUUID().toString().substring(0, 8);
                            FilterDefinition newFilter = new FilterDefinition(
                                    newId,
                                    "Custom Filter",
                                    Color.WHITE,
                                    Priority.NORMAL,
                                    true,
                                    new ArrayList<>()
                            );
                            filters.put(newId, newFilter);
                            FilterRegistry.loadFromConfig();
                            ChiselmonConfig.save();
                            screen.onClose();
                            Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                            mc.setScreen(ChiselmonConfig.createScreen(parent));
                        })
                        .build())
                .build());

        return builder.build();
    }

    private OptionGroup buildGroupOptions(Screen parent, FilterDefinition def) {
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(def.id);
        Component filterName = isDefaultFilter
                ? modTranslatable("config.filters." + def.id)
                : Component.literal(def.id.replace("_", " "));

        var groupBuilder = OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.filters.group.description")));

        // Enabled toggle
        groupBuilder.option(OptionFactory.toggleOnOff(
                "config.filters.enabled",
                () -> def.enabled,
                v -> {
                    def.enabled = v;
                    FilterRegistry.loadFromConfig();
                }
        ));

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
                    .action((screen, opt) -> {
                        filters.remove(def.id);
                        FilterRegistry.loadFromConfig();
                        ChiselmonConfig.save();
                        screen.onClose();
                        Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        mc.setScreen(ChiselmonConfig.createScreen(parent));
                    })
                    .build());
        }

        return groupBuilder.build();
    }

    // New method: Add tags as a separate ListOption group
    private ListOption<String> buildTagsListGroup(FilterDefinition def) {
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(def.id);
        Component filterName = isDefaultFilter
                ? modTranslatable("config.filters." + def.id + ".tags")
                : Component.literal(def.id.replace("_", " ") + " Tags");

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