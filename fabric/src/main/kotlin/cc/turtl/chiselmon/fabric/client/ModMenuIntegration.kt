package cc.turtl.chiselmon.fabric.client

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

object ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            ChiselmonConfig.createScreen(parent)
        }
    }
}