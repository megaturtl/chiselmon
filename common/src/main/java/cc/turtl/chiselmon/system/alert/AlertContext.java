package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.config.category.AlertsConfig;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

public record AlertContext(
        PokemonEntity entity,
        RuntimeFilter filter,
        boolean isMuted,
        AlertsConfig config
) {
    public Pokemon pokemon() {
        return entity.getPokemon();
    }

    private AlertsConfig.FilterAlertSettings getFilterSettings() {
        return config.filterAlerts.computeIfAbsent(filter.id(), id -> new AlertsConfig.FilterAlertSettings());
    }

    public boolean shouldAlert() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return config.masterEnabled && settings.enabled;
    }

    public boolean shouldRepeatingSound() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return shouldAlert() && !isMuted && settings.playSound &&settings.repeatSound;
    }

    public boolean shouldSingleSound() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return shouldAlert() && !isMuted && settings.playSound && !settings.repeatSound;
    }

    public boolean shouldMessage() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return shouldAlert() && !isMuted && settings.sendChatMessage;
    }

    public boolean shouldHighlight() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return shouldAlert() && settings.highlightEntity;
    }

    public float getEffectiveVolume() {
        AlertsConfig.FilterAlertSettings settings = getFilterSettings();
        return (config.masterVolume / 100f) * (settings.volume / 100f);
    }

    public float getEffectivePitch() {
        return 1.0f;
    }
}
