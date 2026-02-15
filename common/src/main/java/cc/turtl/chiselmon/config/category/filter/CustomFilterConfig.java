package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.filter.FilterRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.category.ConfigCategoryBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.*;
import java.util.List;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class CustomFilterConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public Map<String, FilterDefinition> filters = new LinkedHashMap<>();

    // Initialize with defaults on first load
    public CustomFilterConfig() {
        for (FilterDefinition def : DefaultFilters.all()) {
            filters.put(def.id, def);
        }
    }

    @Override
    public ConfigCategory buildCategory() {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.filters"));

        // Build UI for each filter with delete button
        for (FilterDefinition def : filters.values()) {
            builder.group(buildGroupOptions(def));
        }

        // Add a button to create new filters
        builder.group(OptionGroup.createBuilder()
                .name(modTranslatable("config.filters.manage"))
                .option(ButtonOption.createBuilder()
                        .name(modTranslatable("config.filters.add_new"))
                        .description(OptionDescription.of(modTranslatable("config.filters.add_new.description")))
                        .action((screen, opt) -> {
                            // Create a new filter with a unique ID
                            String newId = "custom_filter_" + System.currentTimeMillis();
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
                            // Rebuild and re-open the screen
                            screen.client.setScreen(ChiselmonConfig.createScreen(screen.parent));
                            return true;
                        })
                        .build())
                .build());

        return builder.build();
    }

    private OptionGroup buildGroupOptions(FilterDefinition def) {
        var groupBuilder = OptionGroup.createBuilder()
                .name(Component.literal(def.id.replace("_", " ")))
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

        // List option for tags with add/remove functionality
        groupBuilder.option(ListOption.<String>createBuilder()
                .name(modTranslatable("config.filters.tags"))
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
                .build());

        // Delete button (except for default filters)
        if (!DefaultFilters.all().stream().anyMatch(d -> d.id.equals(def.id))) {
            groupBuilder.option(ButtonOption.createBuilder()
                    .name(modTranslatable("config.filters.delete"))
                    .description(OptionDescription.of(modTranslatable("config.filters.delete.description")))
                    .action((screen, opt) -> {
                        filters.remove(def.id);
                        FilterRegistry.loadFromConfig();
                        ChiselmonConfig.save();
                        // Rebuild and re-open the screen
                        screen.client.setScreen(ChiselmonConfig.createScreen(screen.parent));
                        return true;
                    })
                    .build());
        }

        return groupBuilder.build();
    }
}