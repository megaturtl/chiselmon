package cc.turtl.chiselmon.client.system.alert

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.turtlshell.api.client.ClientEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

object LureAlerter {
    private val SOUND = SoundEvents.GLASS_BREAK

    fun init() {
        ClientEvents.MESSAGE_RECEIVED.subscribe { message: Component ->
            if (!ChiselmonConfig.general.modDisabled && ChiselmonConfig.alert.lureExpiryAlerts) {
                if (message.string.contains("Your lure has run out!")) {
                    Minecraft.getInstance().soundManager.play(
                        SimpleSoundInstance.forUI(SOUND, 0.66f, (ChiselmonConfig.alert.masterVolume / 100f))
                    )
                }
            }
            false
        }
    }
}