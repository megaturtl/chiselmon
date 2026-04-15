package cc.turtl.chiselmon.system.alert

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.config.category.AlertConfig
import cc.turtl.chiselmon.core.api.PokemonEncounter
import cc.turtl.chiselmon.core.api.filter.RuntimeFilter
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Snapshot of a Pokemon's alert state for a single tick.
 *
 * @param entity     The live entity that triggered the alert.
 * @param filters    The filters that matched this pokemon, sorted by priority.
 * @param isMuted    Whether the user has muted this specific pokemon.
 * @param config     The current alert config.
 * @param encounter  The immutable encounter snapshot.
 */
data class AlertContext(
    val entity: PokemonEntity,
    val filters: List<RuntimeFilter>,
    val isMuted: Boolean,
    val config: AlertConfig,
    val encounter: PokemonEncounter,
) {
    val pokemon: Pokemon get() = entity.pokemon

    private fun settingsFor(filter: RuntimeFilter): AlertConfig.FilterAlertSettings =
        config.filterAlerts.getOrPut(filter.id) { AlertConfig.FilterAlertSettings() }

    /** Returns the first filter (in priority order) whose settings satisfy [predicate], or null. */
    private inline fun firstMatching(
        predicate: (AlertConfig.FilterAlertSettings) -> Boolean,
    ): RuntimeFilter? = filters.firstOrNull { predicate(settingsFor(it)) }

    /** The first enabled filter, used for general priority comparisons. */
    val alertFilter: RuntimeFilter? get() = firstMatching { it.enabled }

    /** The filter that drives entity highlighting. */
    val highlightFilter: RuntimeFilter? get() = firstMatching { it.enabled && it.highlightEntity }

    /** The filter that drives chat messages. */
    val messageFilter: RuntimeFilter? get() = firstMatching { it.enabled && it.sendChatMessage }

    /** The filter that drives Discord messages. */
    val discordFilter: RuntimeFilter? get() = firstMatching { it.enabled && it.sendDiscordMessage }

    /** The filter that drives sound (single or repeating). */
    val soundFilter: RuntimeFilter? get() = firstMatching { it.enabled && it.playSound }

    // -------------------------------------------------------------------------
    // Should-X guards
    // -------------------------------------------------------------------------

    val shouldAlert: Boolean
        get() = config.masterEnabled && alertFilter != null

    val shouldRepeatingSound: Boolean
        get() = shouldAlert && !isMuted &&
                firstMatching { it.enabled && it.playSound && it.repeatSound } != null

    val shouldSingleSound: Boolean
        get() = shouldAlert && !isMuted &&
                firstMatching { it.enabled && it.playSound && !it.repeatSound } != null

    val shouldMessage: Boolean
        get() = shouldAlert && !isMuted && messageFilter != null

    val shouldDiscord: Boolean
        get() = shouldAlert && !isMuted &&
                ChiselmonConfig.general.discordWebhookURL.isNotBlank() &&
                discordFilter != null

    val shouldHighlight: Boolean
        get() = shouldAlert && highlightFilter != null

    val discordWebhookUrl: String get() = ChiselmonConfig.general.discordWebhookURL

    /**
     * Settings for the winning sound filter. Returns a default instance if none matched
     * (callers should guard with [shouldSingleSound] / [shouldRepeatingSound] first).
     */
    val soundSettings: AlertConfig.FilterAlertSettings
        get() = soundFilter?.let { settingsFor(it) } ?: AlertConfig.FilterAlertSettings()

    val effectiveVolume: Float
        get() = (config.masterVolume / 100f) * (soundSettings.volume / 100f)
}
