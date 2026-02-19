package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.api.filter.FilterDefinition;
import cc.turtl.chiselmon.api.filter.FiltersUserData;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.system.alert.AlertSounds;
import cc.turtl.chiselmon.data.UserDataRegistry;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.Map;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class AlertsConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public boolean masterEnabled = true;

    @SerialEntry
    public int masterVolume = 100;

    @SerialEntry
    public boolean showFormInMessage = true;

    @SerialEntry
    public Map<String, FilterAlertSettings> filterAlerts = new LinkedHashMap<>();

    @Override
    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(modTranslatable("config.category.alerts"));

        builder.option(OptionFactory.toggleOnOff(
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
                .option(OptionFactory.keyMappingPicker(
                        "config.alerts.mute_keybind",
                        ChiselmonKeybinds.MUTE_ALERTS));

        builder.option(LabelOption.create(modTranslatable("config.alerts.filters")));

        // Add a separate group for each filter's alert settings
        for (FilterDefinition filter : UserDataRegistry.get(FiltersUserData.class).getAll().values()) {
            FilterAlertSettings settings = filterAlerts.computeIfAbsent(
                    filter.id,
                    id -> new FilterAlertSettings()
            );

            builder.group(buildFilterAlertGroup(filter, settings));
        }

        return builder.build();
    }

    private OptionGroup buildFilterAlertGroup(FilterDefinition filter, FilterAlertSettings settings) {
        MutableComponent filterName = ComponentUtils.createComponent(filter.displayName, filter.rgb);

        // Create options and store references to read pending values
        Option<AlertSounds> soundOption = OptionFactory.enumCycler(
                "config.alerts.alert_sound",
                () -> settings.alertSound,
                v -> settings.alertSound = v,
                AlertSounds.class
        );

        Option<Integer> volumeOption = OptionFactory.intSlider(
                "config.alerts.volume",
                () -> settings.volume,
                v -> settings.volume = v,
                0, 100, 1
        );

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
                .option(soundOption)
                .option(ButtonOption.createBuilder()
                        .name(modTranslatable("config.alerts.preview_sound"))
                        .description(OptionDescription.of(modTranslatable("config.alerts.preview_sound.description")))
                        .text(modTranslatable("config.alerts.preview_sound.button"))
                        .action((screen, opt) -> {
                            // Play the currently selected sound from UI (pending value)
                            AlertSounds currentSound = soundOption.pendingValue();
                            int currentVolume = volumeOption.pendingValue();

                            SoundEvent sound = currentSound.getSound();
                            if (sound != null) {
                                Minecraft.getInstance().getSoundManager().play(
                                        SimpleSoundInstance.forUI(sound, 1.0f, currentVolume / 100f)
                                );
                            }
                        })
                        .build())
                .option(volumeOption)
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
        public AlertSounds alertSound = AlertSounds.PLING;

        @SerialEntry
        public int volume = 100;

        @SerialEntry
        public boolean highlightEntity = true;
    }
}