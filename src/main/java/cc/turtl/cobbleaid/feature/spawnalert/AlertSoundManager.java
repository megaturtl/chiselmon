package cc.turtl.cobbleaid.feature.spawnalert;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class AlertSoundManager {
    private final Set<UUID> trackedEntities = new HashSet<>();
    private final Set<UUID> mutedEntities = new HashSet<>();
    private final SpawnAlertConfig config;
    private int tickDelay = 0;

    public AlertSoundManager(SpawnAlertConfig config) {
        this.config = config;
    }

    public void addTarget(UUID uuid) {
        if (!mutedEntities.contains(uuid)) {
            trackedEntities.add(uuid);
        }
    }

    public void muteTarget(UUID uuid) {
        trackedEntities.remove(uuid);
        mutedEntities.add(uuid);
    }

    public void clearTargets() {
        trackedEntities.clear();
        mutedEntities.clear();
    }

    public void removeTarget(UUID uuid) {
        trackedEntities.remove(uuid);
    }

    public void tick() {
        if (trackedEntities.isEmpty() || config.soundVolume <= 0) {
            return;
        }

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        playSound();

        this.tickDelay = config.soundDelay;
    }

    private void playSound() {
        Minecraft mc = Minecraft.getInstance();

        SimpleSoundInstance alert = SimpleSoundInstance.forUI(
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                1.0f,
                config.soundVolume / 100f);

        mc.getSoundManager().play(alert);
    }
}