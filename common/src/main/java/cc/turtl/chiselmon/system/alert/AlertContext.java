package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.AlertConfig;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.function.Predicate;

public record AlertContext(
        PokemonEntity entity,
        List<RuntimeFilter> filters, // sorted by priority they matched for
        boolean isMuted,
        AlertConfig config,
        PokemonEncounter encounter
) {
    public Pokemon pokemon() {
        return entity.getPokemon();
    }

    private AlertConfig.FilterAlertSettings getFilterSettings(RuntimeFilter filter) {
        return config.filterAlerts.computeIfAbsent(filter.id(), id -> new AlertConfig.FilterAlertSettings());
    }

    /**
     * Returns the first filter (in priority order) whose settings satisfy the predicate, or null.
     */
    private RuntimeFilter firstMatchingFilter(Predicate<AlertConfig.FilterAlertSettings> predicate) {
        for (RuntimeFilter filter : filters) {
            if (predicate.test(getFilterSettings(filter))) {
                return filter;
            }
        }
        return null;
    }

    /** The first enabled filter — used for general priority comparisons. */
    public RuntimeFilter alertFilter() {
        return firstMatchingFilter(s -> s.enabled);
    }

    /** The filter that drives highlighting. */
    public RuntimeFilter highlightFilter() {
        return firstMatchingFilter(s -> s.enabled && s.highlightEntity);
    }

    /** The filter that drives chat messages. */
    public RuntimeFilter messageFilter() {
        return firstMatchingFilter(s -> s.enabled && s.sendChatMessage);
    }

    /** The filter that drives Discord messages. */
    public RuntimeFilter discordFilter() {
        return firstMatchingFilter(s -> s.enabled && s.sendDiscordMessage);
    }

    /** The filter that drives sound (single or repeating). */
    public RuntimeFilter soundFilter() {
        return firstMatchingFilter(s -> s.enabled && s.playSound);
    }

    // -------------------------------------------------------------------------
    // Should-X guards
    // -------------------------------------------------------------------------

    public boolean shouldAlert() {
        return config.masterEnabled && alertFilter() != null;
    }

    public boolean shouldRepeatingSound() {
        if (!shouldAlert() || isMuted) return false;
        return firstMatchingFilter(s -> s.enabled && s.playSound && s.repeatSound) != null;
    }

    public boolean shouldSingleSound() {
        if (!shouldAlert() || isMuted) return false;
        return firstMatchingFilter(s -> s.enabled && s.playSound && !s.repeatSound) != null;
    }

    public boolean shouldMessage() {
        return shouldAlert() && !isMuted && messageFilter() != null;
    }

    public boolean shouldDiscord() {
        if (!shouldAlert() || isMuted) return false;
        if (ChiselmonConfig.get().general.discordWebhookURL.isBlank()) return false;
        return discordFilter() != null;
    }

    public boolean shouldHighlight() {
        return shouldAlert() && highlightFilter() != null;
    }

    public String discordWebhookUrl() {
        return ChiselmonConfig.get().general.discordWebhookURL;
    }

    /**
     * Settings for the winning sound filter. Returns a default instance if none matched
     * (callers should guard with shouldSingleSound/shouldRepeatingSound first).
     */
    public AlertConfig.FilterAlertSettings soundSettings() {
        RuntimeFilter filter = soundFilter();
        return filter != null ? getFilterSettings(filter) : new AlertConfig.FilterAlertSettings();
    }

    public float getEffectiveVolume() {
        AlertConfig.FilterAlertSettings settings = soundSettings();
        return (config.masterVolume / 100f) * (settings.volume / 100f);
    }
}