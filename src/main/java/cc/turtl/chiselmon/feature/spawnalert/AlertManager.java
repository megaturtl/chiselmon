package cc.turtl.chiselmon.feature.spawnalert;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.feature.spawnalert.response.AlertResponse;
import cc.turtl.chiselmon.feature.spawnalert.response.handler.AlertChatHandler;
import cc.turtl.chiselmon.feature.spawnalert.response.handler.AlertGlowHandler;
import cc.turtl.chiselmon.feature.spawnalert.response.handler.AlertSoundHandler;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class AlertManager {
    private static final int SOUND_DELAY_TICKS = 20;

    private final Map<UUID, PokemonEntity> loadedUuids = new HashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>();
    private final Set<UUID> messageSentUuids = new HashSet<>();
    private final SpawnAlertConfig config;

    private int soundDelayRemaining = SOUND_DELAY_TICKS;

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void onEntityLoad(Entity entity, ClientLevel level) {
        if (entity instanceof PokemonEntity pe) {
            loadedUuids.put(pe.getUUID(), pe);
        }
    }

    public void onEntityUnload(Entity entity, ClientLevel level) {
        if (entity instanceof PokemonEntity pe) {

            var player = Minecraft.getInstance().player;
            double distance = (player != null) ? player.distanceTo(pe) : 0;
            String reason = (entity.getRemovalReason() != null) ? entity.getRemovalReason().toString() : "UNKNOWN";
            Chiselmon.getLogger().debug(String.format(
                    "PokemonEntity unloaded! '%s' [Reason: %s] lived for %d ticks and was %.2f blocks away.",
                    pe.getPokemon().getSpecies().getName(),
                    reason,
                    pe.getTicksLived(),
                    distance));

            UUID uuid = pe.getUUID();
            loadedUuids.remove(uuid);
            messageSentUuids.remove(uuid);
        }
    }

    public void onDisconnect(ClientPacketListener handler, Minecraft client) {
        loadedUuids.clear();
        messageSentUuids.clear();
        mutedUuids.clear();
    }

    public void onConfigSave(ChiselmonConfig config) {
        // reprocess in case highlights need to be updated etc.
    }

    public void tick() {
        AlertLevel highestSoundLevel = AlertLevel.NONE;

        for (PokemonEntity pe : loadedUuids.values()) {
            UUID uuid = pe.getUUID();

            // mute if busy (battling/catching)
            if (!pe.getBusyLocks().isEmpty()) {
                mutedUuids.add(uuid);
            }

            boolean muted = mutedUuids.contains(uuid);
            AlertResponse response = AlertResponse.calculate(config, pe, muted);

            // if there is no entity or it was skipped in the response (not wild), do nothing but remove glow effects
            if (response.pe() == null) {
                AlertGlowHandler.removeEffects(pe);
                continue;
            }

            // send the message when 1 tick old to hopefully prevent disguises not having the noai tag yet?
            if (!messageSentUuids.contains(uuid) && pe.getTicksLived() > 0) {
                if (!muted) {
                    AlertChatHandler.handle(response, config);
                }
                messageSentUuids.add(uuid);
            }

            AlertGlowHandler.handle(response, config);

            // Track highest sound level from unmuted entities
            if (!muted && response.soundLevel().weight > highestSoundLevel.weight) {
                highestSoundLevel = response.soundLevel();
            }
        }

        // Play sound every x ticks
        if (soundDelayRemaining > 0) {
            soundDelayRemaining--;
        } else {
            AlertSoundHandler.handle(highestSoundLevel, config);
            soundDelayRemaining = SOUND_DELAY_TICKS;
        }
    }

    public void mute(UUID uuid) {
        mutedUuids.add(uuid);
    }

    public void unmuteAll() {
        mutedUuids.clear();
    }

    public void muteAll() {
        mutedUuids.addAll(loadedUuids.keySet());
    }
}