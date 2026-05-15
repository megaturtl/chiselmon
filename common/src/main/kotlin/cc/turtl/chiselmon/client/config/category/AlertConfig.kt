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
    var lureAlert: LureAlertSettings = LureAlertSettings()

    @SerialEntry
    var filterAlerts: MutableMap<String, FilterAlertSettings> = LinkedHashMap()

    fun buildCategory(): ConfigCategory {
        val builder = ConfigCategory.createBuilder()
            .name(Component.translatable("chiselmon.config.category.alerts"))
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

        builder.option(LabelOption.create(Component.translatable("chiselmon.config.alert.filters")))
        for (filter in ChiselmonStorage.FILTERS[Scope.global()].all.values) {
            val settings = filterAlerts.getOrPut(filter.id) { FilterAlertSettings() }
            builder.group(buildFilterAlertGroup(filter, settings))
        }

        builder.group(buildLureAlertGroup(lureAlert))

        return builder.build()
    }

    private fun buildFilterAlertGroup(filter: FilterDefinition, settings: FilterAlertSettings): OptionGroup {
        val filterName = createComponent(filter.displayName, filter.rgb)
        val volumeOption = makeVolumeOption(settings)
        val soundOption = makeSoundOption(settings, volumeOption)

        return OptionGroup.createBuilder()
            .name(filterName)
            .description(
                OptionDescription.of(
                    Component.translatable("chiselmon.config.alert.group.filter_alerts.desc")
                )
            )
            .addBaseAlertOptions(
                settings,
                soundOption,
                volumeOption,
                defaultSendChatMessage = FilterAlertSettings.DEFAULT_SEND_CHAT_MESSAGE_FILTER,
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
                    "chiselmon.config.alert.repeat_sound",
                    FilterAlertSettings.DEFAULT_REPEAT_SOUND,
                    { settings.repeatSound },
                    { settings.repeatSound = it })
            )
            .collapsed(true)
            .build()
    }

    private fun buildLureAlertGroup(settings: LureAlertSettings): OptionGroup {
        val volumeOption = makeVolumeOption(settings)
        val soundOption = makeSoundOption(settings, volumeOption, AlertSounds.GLASS)

        return OptionGroup.createBuilder()
            .name(Component.translatable("chiselmon.config.alert.group.lure_alerts"))
            .description(
                OptionDescription.of(
                    Component.translatable("chiselmon.config.alert.group.lure_alerts.desc")
                )
            )
            .option(
                OptionFactory.toggleOnOff(
                    "chiselmon.config.alert.enabled",
                    AlertSettings.DEFAULT_ENABLED,
                    { settings.enabled },
                    { settings.enabled = it })
            )
            .option(soundOption)
            .option(volumeOption)
            .collapsed(true)
            .build()
    }

    private fun OptionGroup.Builder.addBaseAlertOptions(
        settings: AlertSettings,
        soundOption: Option<AlertSounds>,
        volumeOption: Option<Int>,
        defaultSendChatMessage: Boolean = AlertSettings.DEFAULT_SEND_CHAT_MESSAGE,
    ): OptionGroup.Builder = this
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.alert.enabled",
                AlertSettings.DEFAULT_ENABLED,
                { settings.enabled },
                { settings.enabled = it })
        )
        .option(
            OptionFactory.toggleTick(
                "chiselmon.config.alert.send_chat_message",
                defaultSendChatMessage,
                { settings.sendChatMessage },
                { settings.sendChatMessage = it })
        )
        .option(
            OptionFactory.toggleTick(
                "chiselmon.config.alert.send_discord_message",
                AlertSettings.DEFAULT_SEND_DISCORD_MESSAGE,
                { settings.sendDiscordMessage },
                { settings.sendDiscordMessage = it })
        )
        .option(
            OptionFactory.toggleTick(
                "chiselmon.config.alert.play_sound",
                AlertSettings.DEFAULT_PLAY_SOUND,
                { settings.playSound },
                { settings.playSound = it })
        )
        .option(soundOption)
        .option(volumeOption)

    private fun makeVolumeOption(settings: AlertSettings): Option<Int> =
        OptionFactory.intSlider(
            "chiselmon.config.alert.volume",
            AlertSettings.DEFAULT_VOLUME,
            { settings.volume },
            { settings.volume = it },
            0, 100, 1,
        )

    private fun makeSoundOption(
        settings: AlertSettings,
        volumeOption: Option<Int>,
        default: AlertSounds = AlertSettings.DEFAULT_ALERT_SOUND
    ): Option<AlertSounds> {
        val soundOption = OptionFactory.enumCycler(
            "chiselmon.config.alert.alert_sound",
            default,
            { settings.alertSound },
            { settings.alertSound = it },
            AlertSounds::class.java,
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
        return soundOption
    }

    open class AlertSettings {

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
        var volume: Int = DEFAULT_VOLUME

        companion object {
            const val DEFAULT_ENABLED = true
            const val DEFAULT_SEND_CHAT_MESSAGE = false
            const val DEFAULT_SEND_DISCORD_MESSAGE = false
            const val DEFAULT_PLAY_SOUND = true
            val DEFAULT_ALERT_SOUND = AlertSounds.PLING
            const val DEFAULT_VOLUME = 100
        }
    }

    class FilterAlertSettings : AlertSettings() {

        @SerialEntry
        var repeatSound: Boolean = DEFAULT_REPEAT_SOUND

        @SerialEntry
        var highlightEntity: Boolean = DEFAULT_HIGHLIGHT_ENTITY

        init {
            // Pokemon alerts send a chat message by default but others (like lure expiry) do not
            sendChatMessage = DEFAULT_SEND_CHAT_MESSAGE_FILTER
        }

        companion object {
            const val DEFAULT_SEND_CHAT_MESSAGE_FILTER = true
            const val DEFAULT_REPEAT_SOUND = true
            const val DEFAULT_HIGHLIGHT_ENTITY = true
        }
    }

    class LureAlertSettings : AlertSettings()

    companion object {
        const val DEFAULT_MASTER_ENABLED = false
        const val DEFAULT_MASTER_VOLUME = 100
    }
}