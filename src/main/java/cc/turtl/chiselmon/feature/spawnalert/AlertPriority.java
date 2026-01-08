package cc.turtl.chiselmon.feature.spawnalert;

import net.minecraft.sounds.SoundEvents;

public enum AlertPriority {
    NONE(0, null),
    CUSTOM(1, new AlertSoundProfile(SoundEvents.NOTE_BLOCK_PLING.value(), 1.18f, 1.0f)),
    SIZE(2, new AlertSoundProfile(SoundEvents.NOTE_BLOCK_BIT.value(), 1.0f, 1.0f)),
    SHINY(3, new AlertSoundProfile(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f)),
    LEGENDARY(4, new AlertSoundProfile(SoundEvents.PLAYER_LEVELUP, 1.0f, 0.8f));

    public final int weight;
    public final AlertSoundProfile soundProfile;

    AlertPriority(int weight, AlertSoundProfile soundProfile) {
        this.weight = weight;
        this.soundProfile = soundProfile;
    }
}