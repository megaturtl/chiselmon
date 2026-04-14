package cc.turtl.chiselmon.client.util

import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.chiselmon.core.util.format.labelled
import cc.turtl.turtlshell.api.core.format.ColorLib
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
        sendPrefixed(player, createComponent(message, ColorLib.WHITE.rgb))
    }

    fun sendSuccess(player: LocalPlayer, message: String) {
        sendPrefixed(player, createComponent(message, ColorLib.GREEN.rgb))
    }

    fun sendWarning(player: LocalPlayer, message: String) {
        sendPrefixed(player, createComponent(message, ColorLib.YELLOW.rgb))
    }

    fun sendError(player: LocalPlayer, e: Exception) {
        sendPrefixed(
            player,
            createComponent("An error occurred with that command.", ColorLib.RED.rgb)
        )
        ChiselmonConstants.LOGGER.error("Error occured while executing command: ", e)
    }

    fun sendLabeled(player: LocalPlayer, label: String, value: Any) {
        sendPrefixed(player, labelled(label, value))
    }
