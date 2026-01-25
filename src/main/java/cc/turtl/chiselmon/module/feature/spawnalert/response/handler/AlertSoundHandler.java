package cc.turtl.chiselmon.module.feature.spawnalert.response.handler;

import cc.turtl.chiselmon.module.feature.spawnalert.AlertLevel;
import cc.turtl.chiselmon.module.feature.spawnalert.SpawnAlertConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class AlertSoundHandler {
    public static void handle(AlertLevel level, SpawnAlertConfig config) {
        float volume = level.getVolume(config);
        if (!level.shouldSound(config) || volume <= 0) {
            return;
        }

        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(level.getSound(), level.getPitch(), volume));
    }
}
