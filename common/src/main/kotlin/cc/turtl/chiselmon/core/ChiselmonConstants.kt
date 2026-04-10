package cc.turtl.chiselmon.core

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.chiselmon.util.format.ComponentUtils.createComponent
import cc.turtl.turtlshell.api.core.Platform
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import org.apache.logging.log4j.Logger

object ChiselmonConstants {

    /** The path to the '.minecraft/config/chiselmon' folder */
    @JvmField
    val CONFIG_PATH: Path = Platform.getConfigDir().resolve(BuildDetails.MOD_ID)

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(BuildDetails.MOD_ID)

    @JvmField
    val MESSAGE_PREFIX: Component = Component.empty()
        .append(createComponent("[", ColorUtils.DARK_GRAY.rgb))
        .append(
            Component.literal("\uD83D\uDEE0")
                .withColor(ColorUtils.PINK.rgb)
                .withStyle(ChatFormatting.BOLD)
        )
        .append(createComponent("] ", ColorUtils.DARK_GRAY.rgb))
        .withStyle { style ->
            style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    createComponent("Chiselmon", ColorUtils.PINK.rgb)
                )
            )
        }
}