package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.config.category.filter.DefaultFilters;
import cc.turtl.chiselmon.config.category.filter.FilterDefinition;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;
import static java.util.stream.Collectors.toSet;

public class AlertsConfig implements ConfigCategoryBuilder {

    @SerialEntry(comment = "Master switch for all pokemon alerts")
    public boolean masterEnabled = true;

    @SerialEntry(comment = "Volume control for all alert sounds (0-100)")
    public int masterVolume = 100;

    @SerialEntry(comment = "Show pokemon form in alert messages")
    public boolean showFormInMessage = true;

    @SerialEntry(comment = "Per-filter alert settings")
    public Map<String, FilterAlertSettings> filterAlerts = new LinkedHashMap<>();
    
    // Cache of default filter IDs for O(1) lookup
    private static final Set<String> DEFAULT_FILTER_IDS = DefaultFilters.all().stream()
            .map(f -> f.id)
            .collect(toSet());

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.alerts"));

        // Master settings group
        builder.group(OptionGroup.createBuilder()
                .name(modTranslatable("config.alerts.master_settings"))
                .option(OptionFactory.toggleOnOff(
                        "config.alerts.master_enabled",
                        () -> masterEnabled,
                        v -> masterEnabled = v
                ))
                .option(OptionFactory.intSlider(
                        "config.alerts.master_volume",
                        () -> masterVolume,
                        v -> masterVolume = v,
                        0, 100, 1
                ))
                .option(OptionFactory.toggleTick(
                        "config.alerts.show_form_in_message",
                        () -> showFormInMessage,
                        v -> showFormInMessage = v
                ))
                .build());

        // Add a separate group for each filter's alert settings
        for (FilterDefinition filter : ChiselmonConfig.get().filter.filters.values()) {
            if (filter.enabled) {
                FilterAlertSettings settings = filterAlerts.computeIfAbsent(
                        filter.id,
                        id -> new FilterAlertSettings()
                );
                
                builder.group(buildFilterAlertGroup(filter, settings));
            }
        }

        return builder.build();
    }

    private OptionGroup buildFilterAlertGroup(FilterDefinition filter, FilterAlertSettings settings) {
        // For default filters, use translation. For custom filters, use literal name
        boolean isDefaultFilter = DEFAULT_FILTER_IDS.contains(filter.id);
        Component filterName = isDefaultFilter 
                ? modTranslatable("config.filters." + filter.id)
                : Component.literal(filter.id.replace("_", " "));
        
        return OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(modTranslatable("config.alerts.group.filter_alerts.description")))
                .option(OptionFactory.toggleOnOff(
                        "config.alerts.enabled",
                        () -> settings.enabled,
                        v -> settings.enabled = v
                ))
                .option(OptionFactory.toggleTick(
                        "config.alerts.send_chat_message",
                        () -> settings.sendChatMessage,
                        v -> settings.sendChatMessage = v
                ))
                .option(OptionFactory.toggleTick(
                        "config.alerts.play_sound",
                        () -> settings.playSound,
                        v -> settings.playSound = v
                ))
                .option(OptionFactory.intSlider(
                        "config.alerts.volume",
                        () -> settings.volume,
                        v -> settings.volume = v,
                        0, 100, 1
                ))
                .option(OptionFactory.toggleTick(
                        "config.alerts.highlight_entity",
                        () -> settings.highlightEntity,
                        v -> settings.highlightEntity = v
                ))
                .collapsed(true)
                .build();
    }

    public static class FilterAlertSettings {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean sendChatMessage = true;

        @SerialEntry
        public boolean playSound = true;

        @SerialEntry
        public int volume = 100;

        @SerialEntry
        public boolean highlightEntity = true;
    }
}