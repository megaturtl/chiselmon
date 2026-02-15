package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.api.filter.match.FilterMatchResult;
import cc.turtl.chiselmon.api.filter.match.FilterMatcher;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.AlertsConfig;
import cc.turtl.chiselmon.system.alert.action.AlertAction;
import cc.turtl.chiselmon.system.alert.action.GlowAction;
import cc.turtl.chiselmon.system.alert.action.MessageAction;
import cc.turtl.chiselmon.system.alert.action.SoundAction;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.*;

public class AlertTicker {
    private static final int SOUND_DELAY_TICKS = 20;

    private final List<AlertAction> oneTimeActions;
    private final List<AlertAction> continuousActions;
    private final AlertAction soundAction;
    private final PokemonAlertSystem alerter;
    private final Set<UUID> actionedUuids = new HashSet<>();
    private int soundDelayRemaining = 0;

    public AlertTicker (PokemonAlertSystem alerter) {
        this.alerter = alerter;
        this.oneTimeActions = List.of(new MessageAction());
        this.continuousActions = List.of(new GlowAction());
        this.soundAction = new SoundAction();
    }

    public void tick() {
        AlertsConfig config = ChiselmonConfig.get().alerts;
        if (!config.masterEnabled) return;

        // Track the "best" group match for the sound this tick
        AlertContext bestSoundContext = null;

        for (PokemonEntity pe : alerter.getLoadedPokemonEntities()) {
            UUID uuid = pe.getUUID();
            if (!pe.getBusyLocks().isEmpty()) alerter.mute(uuid);

            // Find the highest priority filter for this Pokemon
            FilterMatchResult result = FilterMatcher.match(pe.getPokemon());
            if (result.primaryMatch().isEmpty()) continue;

            // Create the Context
            AlertContext ctx = new AlertContext(pe, result.primaryMatch().get(), alerter.isMuted(uuid), config);

            continuousActions.forEach(action -> action.execute(ctx));

            if (!actionedUuids.contains(uuid)) {
                oneTimeActions.forEach(action -> action.execute(ctx));
                actionedUuids.add(uuid);
            }

            // Only play sound if not muted and it's higher priority than the current "best"
            if (ctx.shouldSound()) {
                if (bestSoundContext == null || ctx.filter().priority().isHigherThan(bestSoundContext.filter().priority())) {
                    bestSoundContext = ctx;
                }
            }
        }

        handleSoundDelay(bestSoundContext);
    }

    private void handleSoundDelay(AlertContext bestSound) {
        if (soundDelayRemaining > 0) {
            soundDelayRemaining--;
        } else if (bestSound != null) {
            soundAction.execute(bestSound);
            soundDelayRemaining = SOUND_DELAY_TICKS;
        }
    }

    public void clear() {
        actionedUuids.clear();
        soundDelayRemaining = 0;
    }
}