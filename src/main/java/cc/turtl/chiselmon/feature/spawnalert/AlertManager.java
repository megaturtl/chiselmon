package cc.turtl.chiselmon.feature.spawnalert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.api.entity.ClientGlowEntity;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

public class AlertManager {
    private final HashMap<UUID, PokemonEntity> loaded = new HashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>(); // persistent muted cache
    private final Set<UUID> sentMessageUuids = new HashSet<>();
    private final SpawnAlertConfig config;

    private int soundDelayRemaining = 20;
    private final int SOUND_DELAY_TICKS = 20;
    private final int SYNC_DELAY_TICKS = 3; // wait before firing the message (in case of sync lag)

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void tick() {
        AlertType globalHighestAlertType = AlertType.NONE;

        for (PokemonEntity pe : loaded.values()) {
            // wait until the entity is at least a few ticks old to ensure data is synced
            if (pe.getTicksLived() < SYNC_DELAY_TICKS) {
                continue;
            }

            // clean up any non wild pokemon/plushies (shinies bypass the level 1 filter)
            if (!PokemonEntityPredicates.IS_WILD.test(pe)
                    || (config.suppressPlushies && pe.getPokemon().getLevel() == 1
                            && !pe.getPokemon().getShiny())) {
                // remove glow and highlight
                removeEffects(pe);
                continue;
            }

            AlertType highestAlertType = AlertType.getWinningAlertType(config, pe);

            // set the glow and name highlight based on alert type
            setGlow(pe, highestAlertType);
            setNameHighlight(pe, highestAlertType);

            // highlight red if eligible to despawn
            if (config.despawnTrackEnabled && highestAlertType != AlertType.LEGENDARY && highestAlertType != AlertType.SHINY) {
                DespawnTrack.despawnHighlight(pe);
            }

            // if in battle or being caught, mute
            if (!pe.getBusyLocks().isEmpty()) {
                mutedUuids.add(pe.getUUID());
            }

            // send chat message if not done already
            if (!sentMessageUuids.contains(pe.getUUID()) && highestAlertType.shouldSendChatMessage(config)) {
                AlertMessage.sendChatAlert(pe, config.showFormInMessage);
                sentMessageUuids.add(pe.getUUID());
            }

            boolean muted = mutedUuids.contains(pe.getUUID());

            // keep track of the highest priority alert type to play sound for so far
            if (!muted && highestAlertType.shouldPlaySound(config)) {
                if (globalHighestAlertType == AlertType.NONE
                        || highestAlertType.getWeight(config) > globalHighestAlertType.getWeight(config)) {
                    globalHighestAlertType = highestAlertType;
                }
            }
        }

        handleSound(globalHighestAlertType);
    }

    private void removeEffects(PokemonEntity pe) {
        setGlow(pe, null);
        setNameHighlight(pe, null);
    }

    private void setGlow(PokemonEntity entity, AlertType type) {
        if (entity instanceof ClientGlowEntity glowable) {
            if (type != null && type != AlertType.NONE && type.shouldHighlightEntity(config)) {
                glowable.chiselmon$setClientGlowColor(type.getHighlightColor(config));
                glowable.chiselmon$setClientGlowing(true);
            } else {
                glowable.chiselmon$setClientGlowing(false);
            }
        }
    }

    private void setNameHighlight(PokemonEntity entity, AlertType type) {
        if (type != null && type != AlertType.NONE) {
            int rgb = type.getHighlightColor(config);
            char mcColorChar = ColorUtil.getClosestMcColor(rgb);
            String speciesName = entity.getPokemon().getSpecies().getName();
            String formattedName = "§" + mcColorChar + "§l" + speciesName;

            entity.getPokemon().setNickname(Component.literal(formattedName));
        } else {
            entity.getPokemon().setNickname(null);
        }
    }

    private void handleSound(AlertType type) {
        if (type == AlertType.NONE)
            return;
        if (soundDelayRemaining > 0) {
            soundDelayRemaining--;
        } else {
            playSound(type);
            soundDelayRemaining = SOUND_DELAY_TICKS;
        }
    }

    private void playSound(AlertType type) {
        Minecraft mc = Minecraft.getInstance();
        float volume = type.getVolume(config);
        if (volume <= 0)
            return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(type.getSound(), type.getPitch(), volume));
    }

    public void mute(UUID uuid) {
        mutedUuids.add(uuid);
    }

    public void unmuteAll() {
        mutedUuids.clear();
    }

    public void muteAll() {

        // every uuid that has sent a message becomes muted
        mutedUuids.addAll(sentMessageUuids);
    }

    public HashMap<UUID, PokemonEntity> getLoaded() {
        return loaded;
    }
}