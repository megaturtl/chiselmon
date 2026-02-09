package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.system.alert.AlertContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public class SoundAction implements AlertAction {
    @Override
    public void execute(AlertContext ctx) {
        if (!ctx.shouldSound()) return;

        var config = ctx.groupConfig();
        SoundEvent sound = config.sound.getSound();

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