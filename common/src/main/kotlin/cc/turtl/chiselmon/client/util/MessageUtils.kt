package cc.turtl.chiselmon.client.util

import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.chiselmon.util.format.ComponentUtils
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component

    fun executeCommand(player: LocalPlayer, command: String) {
        player.connection.sendCommand(if (command.startsWith("/")) command.substring(1) else command)
    }

    fun sendEmptyLine(player: LocalPlayer) {
        send(player, Component.empty())
    }

    fun send(player: LocalPlayer, message: Component) {
        player.sendSystemMessage(message)
    }

    fun send(player: LocalPlayer, message: String) {
        send(player, Component.literal(message))
    }

    fun sendPrefixed(player: LocalPlayer, message: Component) {
        send(player, Component.empty().append(ChiselmonConstants.MESSAGE_PREFIX).append(message))
    }

    fun sendPrefixed(player: LocalPlayer, message: String) {
        sendPrefixed(player, ComponentUtils.createComponent(message, ColorUtils.WHITE.rgb))
    }

    fun sendSuccess(player: LocalPlayer, message: String) {
        sendPrefixed(player, ComponentUtils.createComponent(message, ColorUtils.GREEN.rgb))
    }

    fun sendWarning(player: LocalPlayer, message: String) {
        sendPrefixed(player, ComponentUtils.createComponent(message, ColorUtils.YELLOW.rgb))
    }

    fun sendError(player: LocalPlayer, e: Exception) {
        sendPrefixed(
            player,
            ComponentUtils.createComponent("An error occurred with that command.", ColorUtils.RED.rgb)
        )
        ChiselmonConstants.LOGGER.error("Error occured while executing command: ", e)
    }

    fun sendLabeled(player: LocalPlayer, label: String, value: Any) {
        sendPrefixed(player, ComponentUtils.labelled(label, value))
    }
