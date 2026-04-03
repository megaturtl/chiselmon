package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.BuildDetails
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object ChiselmonKeybindsKt {
    val OPEN_CONFIG = KeyMapping(
        "chiselmon.key.open_config",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_SEMICOLON,
        BuildDetails.MOD_DISPLAY_NAME
    )

    val MUTE_ALERTS = KeyMapping(
        "chiselmon.key.mute_alerts",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        BuildDetails.MOD_DISPLAY_NAME
    )

    val ALL: List<KeyMapping> = listOf(OPEN_CONFIG)
}