package cc.turtl.chiselmon.client.config.category

import cc.turtl.turtlshell.api.client.config.OptionFactory
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.config.v2.api.SerialEntry
import net.minecraft.network.chat.Component

class RecorderConfig {

    @SerialEntry
    var actionBar: Boolean = DEFAULT_ACTION_BAR

    @SerialEntry
    var despawnGlow: Boolean = DEFAULT_DESPAWN_GLOW

    fun buildCategory(): ConfigCategory = ConfigCategory.createBuilder()
        .name(Component.translatable("chiselmon.config.category.recorder"))
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.recorder.action_bar",
                DEFAULT_ACTION_BAR,
                { actionBar },
                { actionBar = it })
        )
        .option(
            OptionFactory.toggleOnOff(
                "chiselmon.config.recorder.despawn_glow",
                DEFAULT_DESPAWN_GLOW,
                { despawnGlow },
                { despawnGlow = it })
        )
        .build()

    companion object {
        const val DEFAULT_ACTION_BAR = true
        const val DEFAULT_DESPAWN_GLOW = false
    }
}