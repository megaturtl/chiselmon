package cc.turtl.chiselmon.config.category.filter;

import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.category.ConfigCategoryBuilder;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        // Build UI for each filter
        for (FilterDefinition def : filters.values()) {
            builder.group(buildGroupOptions(def));
        }

        return builder.build();
    }

    private OptionGroup buildGroupOptions(FilterDefinition def) {
        return OptionGroup.createBuilder()
                .name(modTranslatable("config.filters." + def.id))
                .option(OptionFactory.toggleOnOff(
                        "config.filters." + def.id + ".enabled",
                        () -> def.enabled,
                        v -> def.enabled = v
                ))
                .option(OptionFactory.colorPicker(
                        "config.filters." + def.id + ".color",
                        () -> def.color,
                        v -> def.color = v
                ))
                .option(OptionFactory.enumDropdown(
                        "config.filters." + def.id + ".priority",
                        () -> def.priority,
                        v -> def.priority = v,
                        cc.turtl.chiselmon.api.Priority.class
                ))
                // Tags as a single text field (users can edit JSON for complex filters)
                .option(OptionFactory.textField(
                        "config.filters." + def.id + ".tags",
                        () -> String.join(", ", def.tags),
                        v -> def.tags = List.of(v.split(",\\s*"))
                ))
                .build();
    }
}