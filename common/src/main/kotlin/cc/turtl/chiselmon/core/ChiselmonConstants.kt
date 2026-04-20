package cc.turtl.chiselmon.core

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.turtlshell.api.core.Platform
import cc.turtl.turtlshell.api.core.format.ColorLib
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path

object ChiselmonConstants {

    /** The path to the '.minecraft/config/chiselmon' folder */
    val CONFIG_PATH: Path = Platform.getConfigDir().resolve(BuildDetails.MOD_ID)

    val LOGGER: Logger = LogManager.getLogger(BuildDetails.MOD_ID)

    val MESSAGE_PREFIX: Component = Component.empty()
        .append(createComponent("[", ColorLib.DARK_GRAY.rgb))
        .append(
            Component.literal("\uD83D\uDEE0")
                .withColor(ColorLib.PINK.rgb)
                .withStyle(ChatFormatting.BOLD)
        )
        .append(createComponent("] ", ColorLib.DARK_GRAY.rgb))
        .withStyle { style ->
            style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    createComponent("Chiselmon", ColorLib.PINK.rgb)
                )
            )
        }
}
