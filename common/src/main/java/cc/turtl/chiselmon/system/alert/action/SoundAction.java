package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.client.config.category.AlertConfig;
import cc.turtl.chiselmon.system.alert.AlertContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class SoundAction implements AlertAction {

    @Override
    public void execute(AlertContext ctx) {
        if (ctx.shouldSingleSound()) playSound(ctx);
    }

    public void executeRepeating(AlertContext ctx) {
        if (ctx.shouldRepeatingSound()) playSound(ctx);
    }

    private void playSound(AlertContext ctx) {
        AlertConfig.FilterAlertSettings settings = ctx.soundSettings();
        SoundEvent sound = settings.getAlertSound().getSound();
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(sound, 1.0f, ctx.getEffectiveVolume())
            );
        }
    }
}