package cc.turtl.chiselmon.client.config

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.client.config.category.*
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.turtlshell.api.client.config.custom.KeyAdapter
import com.mojang.blaze3d.platform.InputConstants
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.minecraft.resources.ResourceLocation

object ChiselmonConfigHandler {

    private val HANDLER: ConfigClassHandler<ConfigData> =
        ConfigClassHandler.createBuilder(ConfigData::class.java)
            .id(ResourceLocation.fromNamespaceAndPath(BuildDetails.MOD_ID, "config"))
            .serializer { config ->
                GsonConfigSerializerBuilder.create(config)
                    .setPath(ChiselmonConstants.CONFIG_PATH.resolve("config.json"))
                    .appendGsonBuilder {
                        it.setPrettyPrinting()
                            .registerTypeHierarchyAdapter(
                                InputConstants.Key::class.java,
                                KeyAdapter()
                            )
                    }
                    .build()
            }
            .build()

    val general get() = HANDLER.instance().general
    val pc get() = HANDLER.instance().pc
    val alert get() = HANDLER.instance().alert
    val recorder get() = HANDLER.instance().recorder
    val filter get() = HANDLER.instance().filter

    fun load() = HANDLER.load()
    fun save() = HANDLER.save()

    class ConfigData {
        @SerialEntry
        val general = GeneralConfig()

        @SerialEntry
        val pc = PCConfig()

        @SerialEntry
        val alert = AlertConfig()

        @SerialEntry
        val recorder = RecorderConfig()
        val filter = FilterConfig()
    }
}