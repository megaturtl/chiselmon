package cc.turtl.chiselmon.client.system.alert

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.turtlshell.api.client.ClientEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component

object LureAlerter {
    fun init() {
        ClientEvents.MESSAGE_RECEIVED.subscribe { message: Component ->
            val alertConfig = ChiselmonConfig.alert

            if (!ChiselmonConfig.general.modDisabled
                && alertConfig.masterEnabled
                && alertConfig.lureAlert.enabled
                && message.string.contains("§cYour lure has run out!")
            ) {

                val volume = (alertConfig.masterVolume / 100f) * (alertConfig.lureAlert.volume / 100f)
                Minecraft.getInstance().soundManager.play(
                    SimpleSoundInstance.forUI(alertConfig.lureAlert.alertSound.sound, 1.0f, volume)
                )
            }
            false // don't cancel the message
        }
    }
}