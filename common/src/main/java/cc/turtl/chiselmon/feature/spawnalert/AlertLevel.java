package cc.turtl.chiselmon.feature.spawnalert;

import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig.AlertConfig;
import cc.turtl.chiselmon.util.ColorUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AlertLevel {
    LEGENDARY(4, SoundEvents.PLAYER_LEVELUP, 1.0f),
    SHINY(3, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f),
    SIZE(2, SoundEvents.NOTE_BLOCK_BIT.value(), 1.0f),
    LIST(1, SoundEvents.NOTE_BLOCK_PLING.value(), 1.18f),
    NONE(0, null, 0.0f);

    public final int weight;
    private final SoundEvent defaultSound;
    private final float defaultPitch;

    AlertLevel(int weight, SoundEvent sound, float pitch) {
        this.weight = weight;
        this.defaultSound = sound;
        this.defaultPitch = pitch;
    }

    private AlertConfig getConfig(SpawnAlertConfig config) {
        return switch (this) {
            case LEGENDARY -> config.legendary;
            case SHINY -> config.shiny;
            case SIZE -> config.size;
            case LIST -> config.list;
            default -> null;
        };
    }

    public boolean isEnabled(SpawnAlertConfig config) {
        return switch (this) {
            case NONE -> false;
            default -> {
                AlertConfig ac = getConfig(config);
                yield ac != null && ac.enabled;
            }
        };
    }

    public boolean shouldChat(SpawnAlertConfig config) {
        AlertConfig ac = getConfig(config);
        return isEnabled(config) && ac.sendChatMessage;
    }

    public boolean shouldSound(SpawnAlertConfig config) {
        AlertConfig ac = getConfig(config);
        return isEnabled(config) && ac.playSound;
    }

    public boolean shouldGlow(SpawnAlertConfig config) {
        AlertConfig ac = getConfig(config);
        return isEnabled(config) && ac.highlightEntity;
    }

    public float getVolume(SpawnAlertConfig config) {
        AlertConfig ac = getConfig(config);
        if (ac == null)
            return 0;
        return (config.masterVolume / 100f) * (ac.volume / 100f);
    }

    public SoundEvent getSound() {
        return defaultSound;
    }

    public float getPitch() {
        return defaultPitch;
    }

    public int getColor(SpawnAlertConfig config) {
        AlertConfig ac = getConfig(config);
        return ac != null ? ac.highlightColor : ColorUtil.WHITE;
    }
}