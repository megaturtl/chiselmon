package cc.turtl.chiselmon.client.system.alert.action

import cc.turtl.chiselmon.client.system.alert.AlertContext
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance

class SoundAction {

    fun execute(ctx: AlertContext) {
        if (ctx.shouldSingleSound) playSound(ctx)
    }

    fun executeRepeating(ctx: AlertContext) {
        if (ctx.shouldRepeatingSound) playSound(ctx)
    }

    private fun playSound(ctx: AlertContext) {
        val sound = ctx.soundSettings.alertSound.sound
        Minecraft.getInstance().soundManager.play(
            SimpleSoundInstance.forUI(sound, 1.0f, ctx.effectiveVolume)
        )
    }
}
