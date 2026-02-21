package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.config.category.AlertsConfig;
import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.system.alert.AlertSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class SoundAction implements AlertAction {
    @Override
    public void execute(AlertContext ctx) {
        if (ctx.shouldSingleSound()) {
            playSound(ctx);
        }
    }

    // so we can use the soundaction for repeating as well
    public void executeRepeating(AlertContext ctx) {
        if (ctx.shouldRepeatingSound()) {
            playSound(ctx);
        }
    }

    public void playSound(AlertContext ctx) {
        // Get the filter's alert settings to retrieve the chosen sound
        AlertsConfig.FilterAlertSettings settings = ctx.config().filterAlerts
                .computeIfAbsent(ctx.filter().id(), id -> new AlertsConfig.FilterAlertSettings());

        // Use the configured alert sound, or default to PLING if null
        AlertSounds alertSound = settings.alertSound != null ? settings.alertSound : AlertSounds.PLING;
        SoundEvent sound = alertSound.getSound();

        if (sound != null) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            sound,
                            ctx.getEffectivePitch(),
                            ctx.getEffectiveVolume()
                    )
            );
        }
    }
}