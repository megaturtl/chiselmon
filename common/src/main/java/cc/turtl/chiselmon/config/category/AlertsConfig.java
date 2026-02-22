package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.api.filter.FilterDefinition;
import cc.turtl.chiselmon.config.OptionFactory;
import cc.turtl.chiselmon.ChiselmonStorage;
import cc.turtl.chiselmon.api.storage.StorageScope;
import cc.turtl.chiselmon.system.alert.AlertSounds;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlertsConfig {

    public static final boolean DEFAULT_MASTER_ENABLED = true;
    public static final int DEFAULT_MASTER_VOLUME = 100;
    public static final boolean DEFAULT_SHOW_FORM_IN_MESSAGE = true;

    @SerialEntry
    public boolean masterEnabled = DEFAULT_MASTER_ENABLED;
    @SerialEntry
    public int masterVolume = DEFAULT_MASTER_VOLUME;
    @SerialEntry
    public boolean showFormInMessage = DEFAULT_SHOW_FORM_IN_MESSAGE;
    @SerialEntry
    public Map<String, FilterAlertSettings> filterAlerts = new LinkedHashMap<>();

    public ConfigCategory buildCategory(Screen parent) {
        var builder = ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.alerts"));

        builder.option(OptionFactory.toggleOnOff(
                        "chiselmon.config.alerts.master_enabled",
                        DEFAULT_MASTER_ENABLED,
                        () -> masterEnabled,
                        v -> masterEnabled = v
                ))
                .option(OptionFactory.intSlider(
                        "chiselmon.config.alerts.master_volume",
                        DEFAULT_MASTER_VOLUME,
                        () -> masterVolume,
                        v -> masterVolume = v,
                        0, 100, 1
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.show_form_in_message",
                        DEFAULT_SHOW_FORM_IN_MESSAGE,
                        () -> showFormInMessage,
                        v -> showFormInMessage = v
                ))
                .option(OptionFactory.keyMappingPicker(
                        "chiselmon.config.alerts.mute_keybind",
                        ChiselmonKeybinds.MUTE_ALERTS));

        builder.option(LabelOption.create(Component.translatable("chiselmon.config.alerts.filters")));

        for (FilterDefinition filter : ChiselmonStorage.FILTERS.get(StorageScope.global()).getAll().values()) {
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

        Option<Integer> volumeOption = OptionFactory.intSlider(
                "chiselmon.config.alerts.volume",
                FilterAlertSettings.DEFAULT_VOLUME,
                () -> settings.volume,
                v -> settings.volume = v,
                0, 100, 1
        );

        Option<AlertSounds> soundOption = OptionFactory.enumCycler(
                "chiselmon.config.alerts.alert_sound",
                FilterAlertSettings.DEFAULT_ALERT_SOUND,
                () -> settings.alertSound,
                v -> settings.alertSound = v,
                AlertSounds.class
        );

        soundOption.addEventListener((opt, event) -> {
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                SoundEvent sound = opt.pendingValue().getSound();
                if (sound != null) {
                    float volume = (volumeOption.pendingValue() / 100f) * (masterVolume / 100f);
                    Minecraft.getInstance().getSoundManager().play(
                            SimpleSoundInstance.forUI(sound, 1.0f, volume)
                    );
                }
            }
        });

        return OptionGroup.createBuilder()
                .name(filterName)
                .description(OptionDescription.of(Component.translatable("chiselmon.config.alerts.group.filter_alerts.description")))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.alerts.enabled",
                        FilterAlertSettings.DEFAULT_ENABLED,
                        () -> settings.enabled,
                        v -> settings.enabled = v
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.send_chat_message",
                        FilterAlertSettings.DEFAULT_SEND_CHAT_MESSAGE,
                        () -> settings.sendChatMessage,
                        v -> settings.sendChatMessage = v
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.send_discord_message",
                        FilterAlertSettings.DEFAULT_SEND_DISCORD_MESSAGE,
                        () -> settings.sendDiscordMessage,
                        v -> settings.sendDiscordMessage = v
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.highlight_entity",
                        FilterAlertSettings.DEFAULT_HIGHLIGHT_ENTITY,
                        () -> settings.highlightEntity,
                        v -> settings.highlightEntity = v
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.play_sound",
                        FilterAlertSettings.DEFAULT_PLAY_SOUND,
                        () -> settings.playSound,
                        v -> settings.playSound = v
                ))
                .option(OptionFactory.toggleTick(
                        "chiselmon.config.alerts.repeat_sound",
                        FilterAlertSettings.DEFAULT_REPEAT_SOUND,
                        () -> settings.repeatSound,
                        v -> settings.repeatSound = v
                ))
                .option(soundOption)
                .option(volumeOption)
                .collapsed(true)
                .build();
    }

    public static class FilterAlertSettings {
        public static final boolean DEFAULT_ENABLED = true;
        public static final boolean DEFAULT_SEND_CHAT_MESSAGE = true;
        public static final boolean DEFAULT_SEND_DISCORD_MESSAGE = false;
        public static final boolean DEFAULT_PLAY_SOUND = true;
        public static final AlertSounds DEFAULT_ALERT_SOUND = AlertSounds.PLING;
        public static final boolean DEFAULT_REPEAT_SOUND = true;
        public static final int DEFAULT_VOLUME = 100;
        public static final boolean DEFAULT_HIGHLIGHT_ENTITY = true;

        @SerialEntry public boolean enabled = DEFAULT_ENABLED;
        @SerialEntry public boolean sendChatMessage = DEFAULT_SEND_CHAT_MESSAGE;
        @SerialEntry public boolean sendDiscordMessage = DEFAULT_SEND_DISCORD_MESSAGE;
        @SerialEntry public boolean playSound = DEFAULT_PLAY_SOUND;
        @SerialEntry public AlertSounds alertSound = DEFAULT_ALERT_SOUND;
        @SerialEntry public boolean repeatSound = DEFAULT_REPEAT_SOUND;
        @SerialEntry public int volume = DEFAULT_VOLUME;
        @SerialEntry public boolean highlightEntity = DEFAULT_HIGHLIGHT_ENTITY;
    }
}