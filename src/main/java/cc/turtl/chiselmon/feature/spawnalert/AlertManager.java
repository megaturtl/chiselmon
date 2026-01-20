package cc.turtl.chiselmon.feature.spawnalert;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.api.entity.ClientGlowEntity;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class AlertManager {
    private final Map<UUID, LoadedPokemonWrapper> activePokemon = new LinkedHashMap<>();
    private final Set<UUID> mutedUuids = new HashSet<>(); // persistant muted cache
    private final SpawnAlertConfig config;

    private int soundDelayRemaining = 20;
    private final int SOUND_DELAY_TICKS = 20;
    private final int SYNC_DELAY_TICKS = 5; // wait before firing the message (in case of sync lag)

    public AlertManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void onEntityLoad(PokemonEntity entity) {
        LoadedPokemonWrapper wrapper = new LoadedPokemonWrapper(entity);
        if (mutedUuids.contains(entity.getUUID())) {
            wrapper.muted = true;
        }
        activePokemon.put(entity.getUUID(), wrapper);
    }

    public void onEntityUnload(PokemonEntity entity) {
        activePokemon.remove(entity.getUUID());
    }

    public void tick() {
        if (Minecraft.getInstance().level == null)
            return;

        long currentGameTime = Minecraft.getInstance().level.getGameTime();
        AlertType globalHighestAlertType = AlertType.NONE;

        var iterator = activePokemon.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LoadedPokemonWrapper wrapper = entry.getValue();
            PokemonEntity entity = wrapper.entity;

            // wait until the entity is at least 5 ticks old to ensure data is synced
            long tick_age = currentGameTime - wrapper.loadedGameTime;
            if (tick_age < SYNC_DELAY_TICKS) {
                continue;
            }

            // clean up any non wild pokemon/plushies (shinies bypass the level 1 filter)
            if (!PokemonEntityPredicates.IS_WILD.test(entity)
                    || (config.suppressPlushies && entity.getPokemon().getLevel() == 1
                            && !entity.getPokemon().getShiny())) {
                setGlow(entity, null);
                iterator.remove();
                continue;
            }

            AlertType highestAlertType = wrapper.getWinningAlertType(config);

            // send chat message if not done already
            if (!wrapper.chatMessageSent && !wrapper.muted) {
                if (highestAlertType.shouldSendChatMessage(config)) {
                    AlertMessage.sendChatAlert(wrapper, config.showFormInMessage);
                }
                wrapper.chatMessageSent = true;
            }

            // set the glow based on alert type
            setGlow(entity, highestAlertType);

            // keep track of the highest priority alert type so far
            if (!wrapper.muted && highestAlertType.shouldPlaySound(config)) {
                if (globalHighestAlertType == AlertType.NONE
                        || highestAlertType.getWeight(config) > globalHighestAlertType.getWeight(config)) {
                    globalHighestAlertType = highestAlertType;
                }
            }
        }

        handleSound(globalHighestAlertType);
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

    public LoadedPokemonWrapper getLoaded(UUID uuid) {
        return activePokemon.get(uuid);
    }

    public void removeLoaded(PokemonEntity entity) {
        activePokemon.remove(entity.getUUID());
        setGlow(entity, null);
    }

    public void clearLoaded() {
        mutedUuids.clear();
        activePokemon.clear();
    }

    public void muteLoaded(UUID uuid) {
        mutedUuids.add(uuid);
        LoadedPokemonWrapper target = activePokemon.get(uuid);
        if (target != null) {
            target.muted = true;
        }
    }

    public void muteLoadedByActorId(UUID actorId) {

        Map.Entry<UUID, LoadedPokemonWrapper> targetEntry = activePokemon.entrySet().stream()
                .filter(entry -> entry.getValue().entity.getPokemon().getUuid().equals(actorId))
                .findFirst()
                .orElse(null);

        if (targetEntry != null) {
            mutedUuids.add(targetEntry.getKey());
            targetEntry.getValue().muted = true;
        }
    }

    public void unmuteAll() {
        mutedUuids.clear();
        activePokemon.values().forEach(t -> t.muted = false);
    }

    public void muteAll() {
        activePokemon.forEach((uuid, pokemon) -> {
            mutedUuids.add(uuid);
            pokemon.muted = true;
        });
    }
}