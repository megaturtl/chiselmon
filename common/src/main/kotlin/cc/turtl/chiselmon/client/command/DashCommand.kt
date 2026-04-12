package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.system.tracker.TrackerManager
import cc.turtl.chiselmon.system.tracker.TrackerSession
import cc.turtl.chiselmon.util.MessageUtils
import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.chiselmon.util.format.ComponentUtils
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
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

                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "Chiselmon Dash - Commands")
                MessageUtils.sendPrefixed(player, "  /$root dash status")
                MessageUtils.sendPrefixed(player, "  /$root dash open")
                MessageUtils.sendPrefixed(player, "  /$root dash close")
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
            MessageUtils.sendWarning(player, "Dashboard server is not running.")
            return Command.SINGLE_SUCCESS
        }

        val url = "http://localhost:${session.dashboardPort}/"
        val uptime = formatDuration(session.dashboardUptime())

        MessageUtils.sendPrefixed(
            player, Component.literal("Dashboard server is running at ")
                .withColor(ColorUtils.GREEN.rgb)
                .append(ComponentUtils.clickableUrl(url))
                .append(Component.literal(" (Uptime: $uptime)"))
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeOpen(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        val session = requireSession(player) ?: return Command.SINGLE_SUCCESS

        val url = "http://localhost:${session.dashboardPort}/"

        if (session.isDashboardRunning) {
            MessageUtils.sendPrefixed(
                player, Component.literal("Dashboard is already running at ")
                    .withColor(ColorUtils.YELLOW.rgb)
                    .append(ComponentUtils.clickableUrl(url))
            )
            return Command.SINGLE_SUCCESS
        }

        try {
            session.startDashboard()
            MessageUtils.sendPrefixed(
                player, Component.literal("Dashboard server opened at ")
                    .withColor(ColorUtils.GREEN.rgb)
                    .append(ComponentUtils.clickableUrl(url))
            )
        } catch (e: Exception) {
            MessageUtils.sendError(player, e)
        }

        return Command.SINGLE_SUCCESS
    }

    private fun executeClose(): Int {
        val player = Minecraft.getInstance().player ?: return 0
        val session = requireSession(player) ?: return Command.SINGLE_SUCCESS

        if (!session.isDashboardRunning) {
            MessageUtils.sendWarning(player, "Dashboard server is not running.")
            return Command.SINGLE_SUCCESS
        }

        session.stopDashboard()
        MessageUtils.sendSuccess(player, "Dashboard server closed.")
        return Command.SINGLE_SUCCESS
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private fun requireSession(player: LocalPlayer): TrackerSession? {
        return try {
            TrackerManager.getInstance().tracker
        } catch (e: IllegalStateException) {
            MessageUtils.sendWarning(player, "No active tracker session.")
            null
        }
    }
}