package cc.turtl.cobbleaid.feature.spawnalert;

import net.minecraft.sounds.SoundEvent;

public record AlertSoundProfile(
        SoundEvent sound,
        float pitch,
        float volumeMultiplier) {
}