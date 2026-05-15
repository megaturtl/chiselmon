package cc.turtl.chiselmon.client.config.category

import cc.turtl.chiselmon.client.ChiselmonKeybinds
import cc.turtl.chiselmon.client.ChiselmonStorage
import cc.turtl.chiselmon.client.system.alert.AlertSounds
import cc.turtl.chiselmon.core.api.filter.FilterDefinition
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.turtlshell.api.client.config.OptionFactory
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component

class AlertConfig {

    @SerialEntry
    var masterEnabled: Boolean = DEFAULT_MASTER_ENABLED

    @SerialEntry
    var masterVolume: Int = DEFAULT_MASTER_VOLUME

    @SerialEntry
    var lureExpiryAlerts: Boolean = DEFAULT_LURE_EXPIRY_ALERTS

    @SerialEntry
    var filterAlerts: MutableMap<String, FilterAlertSettings> = LinkedHashMap()

    fun buildCategory(): ConfigCategory {
        val builder = ConfigCategory.createBuilder()
            .name(Component.translatable("chiselmon.config.category.alerts"))

        builder
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.alert.master_enabled",
                    DEFAULT_MASTER_ENABLED,
                    { masterEnabled },
                    { masterEnabled = it })
            )
            .option(
                OptionFactory.intSlider(
                    "chiselmon.config.alert.master_volume",
                    DEFAULT_MASTER_VOLUME,
                    { masterVolume },
                    { masterVolume = it },
                    0, 100, 1
                )
            )
            .option(
                OptionFactory.keyMappingPicker(
                    "chiselmon.config.alert.mute_keybind",
                    ChiselmonKeybinds.MUTE_ALERTS
                )
            )
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.alert.lure_alerts",
                    DEFAULT_LURE_EXPIRY_ALERTS,
                    { lureExpiryAlerts },
                    { lureExpiryAlerts = it })
            )

        builder.option(LabelOption.create(Component.translatable("chiselmon.config.alert.filters")))

        for (filter in ChiselmonStorage.FILTERS[Scope.global()].all.values) {
            val settings = filterAlerts.getOrPut(filter.id) { FilterAlertSettings() }
            builder.group(buildFilterAlertGroup(filter, settings))
        }

        return builder.build()
    }

    private fun buildFilterAlertGroup(filter: FilterDefinition, settings: FilterAlertSettings): OptionGroup {
        val filterName = createComponent(filter.displayName, filter.rgb)

        val volumeOption = OptionFactory.intSlider(
            "chiselmon.config.alert.volume",
            FilterAlertSettings.DEFAULT_VOLUME,
            { settings.volume },
            { settings.volume = it },
            0, 100, 1
        )

        val soundOption = OptionFactory.enumCycler(
            "chiselmon.config.alert.alert_sound",
            FilterAlertSettings.DEFAULT_ALERT_SOUND,
            { settings.alertSound },
            { settings.alertSound = it },
            AlertSounds::class.java
        )

        soundOption.addEventListener { opt, event ->
            if (event == OptionEventListener.Event.STATE_CHANGE) {
                val sound = opt.pendingValue().sound
                val volume = (volumeOption.pendingValue() / 100f) * (masterVolume / 100f)
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(sound, 1.0f, volume)
                )
            }
        }

        return OptionGroup.createBuilder()
            .name(filterName)
            .description(
                OptionDescription.of(
                    Component.translatable("chiselmon.config.alert.group.filter_alerts.desc")
                )
            )
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.alert.enabled",
                    FilterAlertSettings.DEFAULT_ENABLED,
                    { settings.enabled },
                    { settings.enabled = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.alert.send_chat_message",
                    FilterAlertSettings.DEFAULT_SEND_CHAT_MESSAGE,
                    { settings.sendChatMessage },
                    { settings.sendChatMessage = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.alert.send_discord_message",
                    FilterAlertSettings.DEFAULT_SEND_DISCORD_MESSAGE,
                    { settings.sendDiscordMessage },
                    { settings.sendDiscordMessage = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.alert.highlight_entity",
                    FilterAlertSettings.DEFAULT_HIGHLIGHT_ENTITY,
                    { settings.highlightEntity },
                    { settings.highlightEntity = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.alert.play_sound",
                    FilterAlertSettings.DEFAULT_PLAY_SOUND,
                    { settings.playSound },
                    { settings.playSound = it })
            )
            .option(
                OptionFactory.toggleTick(
                    "chiselmon.config.alert.repeat_sound",
                    FilterAlertSettings.DEFAULT_REPEAT_SOUND,
                    { settings.repeatSound },
                    { settings.repeatSound = it })
            )
            .option(soundOption)
            .option(volumeOption)
            .collapsed(true)
            .build()
    }

    class FilterAlertSettings {

        @SerialEntry
        var enabled: Boolean = DEFAULT_ENABLED

        @SerialEntry
        var sendChatMessage: Boolean = DEFAULT_SEND_CHAT_MESSAGE

        @SerialEntry
        var sendDiscordMessage: Boolean = DEFAULT_SEND_DISCORD_MESSAGE

        @SerialEntry
        var playSound: Boolean = DEFAULT_PLAY_SOUND

        @SerialEntry
        var alertSound: AlertSounds = DEFAULT_ALERT_SOUND

        @SerialEntry
        var repeatSound: Boolean = DEFAULT_REPEAT_SOUND

        @SerialEntry
        var volume: Int = DEFAULT_VOLUME

        @SerialEntry
        var highlightEntity: Boolean = DEFAULT_HIGHLIGHT_ENTITY

        companion object {
            const val DEFAULT_ENABLED = true
            const val DEFAULT_SEND_CHAT_MESSAGE = true
            const val DEFAULT_SEND_DISCORD_MESSAGE = false
            const val DEFAULT_PLAY_SOUND = true
            val DEFAULT_ALERT_SOUND = AlertSounds.PLING
            const val DEFAULT_REPEAT_SOUND = true
            const val DEFAULT_VOLUME = 100
            const val DEFAULT_HIGHLIGHT_ENTITY = true
        }
    }

    companion object {
        const val DEFAULT_MASTER_ENABLED = false
        const val DEFAULT_MASTER_VOLUME = 100
        const val DEFAULT_LURE_EXPIRY_ALERTS = false
    }
}