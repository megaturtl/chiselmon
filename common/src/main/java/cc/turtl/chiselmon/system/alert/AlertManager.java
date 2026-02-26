package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.api.PokemonEncounter;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.api.filter.match.FilterMatchResult;
import cc.turtl.chiselmon.api.filter.match.FilterMatcher;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.AlertConfig;
import cc.turtl.chiselmon.system.alert.action.*;
import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.chiselmon.util.MessageUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.*;

public class AlertManager {
    private static final int SOUND_DELAY_TICKS = 20;

    private static final AlertManager INSTANCE = new AlertManager();

    private final List<AlertAction> oneTimeActions = List.of(new MessageAction(), new SoundAction(), new DiscordAction());
    private final List<AlertAction> continuousActions = List.of(new GlowAction());
    private final SoundAction repeatingSoundAction = new SoundAction();

    private Set<UUID> mutedUuids;
    private Set<UUID> actionedUuids;
    private int soundDelayRemaining = 0;
    private boolean active = false;

    private AlertManager() {
    }

    public static AlertManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        ChiselmonEvents.LEVEL_CONNECTED.subscribe(Priority.LOW, e -> onWorldJoin());
        ChiselmonEvents.LEVEL_DISCONNECTED.subscribe(Priority.LOW, e -> onWorldLeave());
        // this is low priority for the glow logic that might clash with despawn glow. i want alert to override despawn glow
        ChiselmonEvents.CLIENT_POST_TICK.subscribe(Priority.LOW, e -> {
            if (active) tick();
        });
        ChiselmonConstants.LOGGER.info("AlertSystem initialized");
    }

    private void onWorldJoin() {
        if (active) {
            ChiselmonConstants.LOGGER.warn("New world joined before AlertSystem was disposed - resetting");
        }
        mutedUuids = new HashSet<>();
        actionedUuids = new HashSet<>();
        soundDelayRemaining = 0;
        active = true;
        ChiselmonConstants.LOGGER.debug("AlertSystem started");
    }

    private void onWorldLeave() {
        mutedUuids = null;
        actionedUuids = null;
        soundDelayRemaining = 0;
        active = false;
        ChiselmonConstants.LOGGER.debug("AlertSystem disposed");
    }

    private void tick() {
        AlertConfig config = ChiselmonConfig.get().alert;
        if (!config.masterEnabled) return;

        while (ChiselmonKeybinds.MUTE_ALERTS.consumeClick()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                muteAll();
                MessageUtils.sendSuccess(player, "All active alert muted");
            }
        }

        // Track the "best" filter match for the sound this tick
        AlertContext bestSoundContext = null;

        for (PokemonEntity pe : getLoadedPokemonEntities()) {
            UUID uuid = pe.getUUID();
            if (!pe.getBusyLocks().isEmpty()) mute(uuid);

            FilterMatchResult result = FilterMatcher.match(pe.getPokemon());
            if (result.allMatches().isEmpty()) continue;

            AlertContext ctx = new AlertContext(pe, result.allMatches(), isMuted(uuid), config, PokemonEncounter.from(pe));

            continuousActions.forEach(action -> action.execute(ctx));

            if (!actionedUuids.contains(uuid)) {
                oneTimeActions.forEach(action -> action.execute(ctx));
                actionedUuids.add(uuid);
            }

            // Update the best sound if the pokemon isn't muted and their filter ctx has a higher priority than the current best
            if (ctx.shouldRepeatingSound()) {
                if (bestSoundContext == null || ctx.soundFilter().priority().isHigherThan(bestSoundContext.soundFilter().priority())) {
                    bestSoundContext = ctx;
                }
            }
        }

        // replay the sound action for repeating sound alert
        if (soundDelayRemaining > 0) {
            soundDelayRemaining--;
        } else if (bestSoundContext != null) {
            repeatingSoundAction.executeRepeating(bestSoundContext);
            soundDelayRemaining = SOUND_DELAY_TICKS;
        }
    }

    private Collection<PokemonEntity> getLoadedPokemonEntities() {
        return TrackerManager.getInstance().getTracker().getCurrentlyLoaded().values();
    }

    public void mute(UUID uuid) {
        mutedUuids.add(uuid);
    }

    public void muteAll() {
        mutedUuids.addAll(TrackerManager.getInstance().getTracker().getCurrentlyLoaded().keySet());
    }

    public void unmuteAll() {
        mutedUuids.clear();
    }

    public boolean isMuted(UUID uuid) {
        return mutedUuids.contains(uuid);
    }

    public Set<UUID> getMutedUuids() {
        return mutedUuids;
    }
}