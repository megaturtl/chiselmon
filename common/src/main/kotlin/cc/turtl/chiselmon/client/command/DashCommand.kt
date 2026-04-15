package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.client.util.*
import cc.turtl.chiselmon.core.util.format.clickableUrl
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import cc.turtl.turtlshell.api.core.format.ColorLib
import cc.turtl.turtlshell.api.core.format.formatDuration
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class DashCommand : TurtlShellCommand {

    override val name = "dash"
    override val description: MutableComponent = Component.literal("View detailed spawning stats in the Chiselmon Dash")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes { ctx ->
                val player = Minecraft.getInstance().player ?: return@executes 0
                val root = ctx.nodes.first().node.name

                sendEmptyLine(player)
                sendSuccess(player, "Chiselmon Dash - Commands")
                sendPrefixed(player, "  /$root dash status")
                sendPrefixed(player, "  /$root dash open")
                sendPrefixed(player, "  /$root dash close")
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("status")
                    .executes { executeStatus() }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("open")
                    .executes { executeOpen() }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("close")
                    .executes { executeClose() }
            )

    private fun executeStatus(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        val session = requireSession(player) ?: return Command.SINGLE_SUCCESS

        if (!session.isDashboardRunning) {
            sendWarning(player, "Dashboard server is not running.")
            return Command.SINGLE_SUCCESS
        }

        val url = "http://localhost:${session.dashboardPort}/"
        val uptime = formatDuration(session.dashboardUptime())

        sendPrefixed(
            player, Component.literal("Dashboard server is running at ")
                .withColor(ColorLib.GREEN.rgb)
                .append(clickableUrl(url))
                .append(Component.literal(" (Uptime: $uptime)"))
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeOpen(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        val session = requireSession(player) ?: return Command.SINGLE_SUCCESS

        val url = "http://localhost:${session.dashboardPort}/"

        if (session.isDashboardRunning) {
            sendPrefixed(
                player, Component.literal("Dashboard is already running at ")
                    .withColor(ColorLib.YELLOW.rgb)
                    .append(clickableUrl(url))
            )
            return Command.SINGLE_SUCCESS
        }

        try {
            session.startDashboard()
            sendPrefixed(
                player, Component.literal("Dashboard server opened at ")
                    .withColor(ColorLib.GREEN.rgb)
                    .append(clickableUrl(url))
            )
        } catch (e: Exception) {
            sendError(player, e)
        }

        return Command.SINGLE_SUCCESS
    }

    private fun executeClose(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        val session = requireSession(player) ?: return Command.SINGLE_SUCCESS

        if (!session.isDashboardRunning) {
            sendWarning(player, "Dashboard server is not running.")
            return Command.SINGLE_SUCCESS
        }

        session.stopDashboard()
        sendSuccess(player, "Dashboard server closed.")
        return Command.SINGLE_SUCCESS
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private fun requireSession(player: LocalPlayer): TrackerSession? {
        return try {
            TrackerSession.current
        } catch (e: IllegalStateException) {
            sendWarning(player, "No active tracker session.")
            null
        }
    }
}