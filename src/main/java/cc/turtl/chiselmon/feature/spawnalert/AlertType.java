package cc.turtl.chiselmon.feature.spawnalert;

import java.util.HashSet;
import java.util.Set;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.ColorUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AlertType {
    LEGENDARY(4, SoundEvents.PLAYER_LEVELUP, 1.0f),
    SHINY(3, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f),
    SIZE(2, SoundEvents.NOTE_BLOCK_BIT.value(), 1.0f),
    LIST(1, SoundEvents.NOTE_BLOCK_PLING.value(), 1.18f),
    NONE(0, null, 0.0f);

    private final int weight;
    private final SoundEvent defaultSound;
    private final float defaultPitch;

    AlertType(int weight, SoundEvent sound, float pitch) {
        this.weight = weight;
        this.defaultSound = sound;
        this.defaultPitch = pitch;
    }

    // --- Data Driven Getters ---

    public boolean isEnabled(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary.enabled;
            case SHINY -> config.shiny.enabled;
            case SIZE -> config.size.enabled;
            case LIST -> config.list.enabled;
            default -> true;
        };
    };

    public int getWeight(SpawnAlertConfig config) {
        return weight;
    };

    public boolean shouldSendChatMessage(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary.sendChatMessage;
            case SHINY -> config.shiny.sendChatMessage;
            case SIZE -> config.size.sendChatMessage;
            case LIST -> config.list.sendChatMessage;
            default -> false;
        };
    }

    public boolean shouldPlaySound(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary.playSound;
            case SHINY -> config.shiny.playSound;
            case SIZE -> config.size.playSound;
            case LIST -> config.list.playSound;
            default -> false;
        };
    }

    public float getVolume(SpawnAlertConfig config) {
        int configVol = switch (this) {
            case LEGENDARY -> config.legendary.volume;
            case SHINY -> config.shiny.volume;
            case SIZE -> config.size.volume;
            case LIST -> config.list.volume;
            default -> 0;
        };
        // Combine master volume with specific category volume
        return (config.masterVolume / 100f) * (configVol / 100f);
    }

    public SoundEvent getSound() {
        return defaultSound;
    }

    public float getPitch() {
        return defaultPitch;
    }

    public boolean shouldHighlightEntity(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary.highlightEntity;
            case SHINY -> config.shiny.highlightEntity;
            case SIZE -> config.size.highlightEntity;
            case LIST -> config.list.highlightEntity;
            default -> false;
        };
    }

    public int getHighlightColor(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary.highlightColor;
            case SHINY -> config.shiny.highlightColor;
            case SIZE -> config.size.highlightColor;
            case LIST -> config.list.highlightColor;
            default -> ColorUtil.WHITE;
        };
    }

    public static AlertType getWinningAlertType(SpawnAlertConfig config, PokemonEntity pe) {
        Pokemon pokemon = pe.getPokemon();

        Set<AlertType> validAlertTypes = new HashSet<>();

        if (PokemonPredicates.IS_SHINY.test(pokemon)) {
            validAlertTypes.add(AlertType.SHINY);
        }
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            validAlertTypes.add(AlertType.SIZE);
        }
        if (!PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
            if (PokemonPredicates.IS_SPECIAL.test(pokemon)) {
                validAlertTypes.add(AlertType.LEGENDARY);
            }
            if (PokemonPredicates.isInCustomList(config.list.whitelist).test(pokemon)) {
                validAlertTypes.add(AlertType.LIST);
            }
        }

        return validAlertTypes.stream()
                .filter(t -> t.isEnabled(config))
                .max((p1, p2) -> Integer.compare(p1.getWeight(config), p2.getWeight(config)))
                .orElse(AlertType.NONE);
    }

}