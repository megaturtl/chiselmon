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
        if (!ctx.shouldSound()) return;

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