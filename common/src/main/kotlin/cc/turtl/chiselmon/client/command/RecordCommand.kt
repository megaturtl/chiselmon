package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderSession
import cc.turtl.chiselmon.client.util.*
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import cc.turtl.turtlshell.api.core.format.ColorLib
import cc.turtl.turtlshell.api.core.format.formatDecimal
import cc.turtl.turtlshell.api.core.format.formatDuration
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class RecordCommand : TurtlShellCommand {

    override val name = "record"
    override val description: MutableComponent = Component.literal("Record pokemon spawn data")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes { ctx ->
                val player = Minecraft.getInstance().player ?: return@executes 0
                val root = ctx.nodes.first().node.name

                sendEmptyLine(player)
                sendSuccess(player, "Spawn Recorder - Commands")
                sendPrefixed(player, "  /$root record start")
                sendPrefixed(player, "  /$root record pause")
                sendPrefixed(player, "  /$root record resume")
                sendPrefixed(player, "  /$root record stop")
                sendPrefixed(player, "  /$root record summary")
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("start")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        try {
                            val started = SpawnRecorderManager.getInstance().startSession()
                            if (!started) {
                                sendWarning(player, "A Spawn Recorder session is already running!")
                            } else {
                                sendSuccess(player, "Spawn Recorder session started!")
                            }
                        } catch (e: Exception) {
                            sendError(player, e)
                        }
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("pause")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val session = requireSession(player) ?: return@executes Command.SINGLE_SUCCESS

                        if (session.isPaused) {
                            sendWarning(player, "Session is already paused.")
                            return@executes Command.SINGLE_SUCCESS
                        }
                        session.pause()
                        sendSuccess(player, "Spawn Recorder paused.")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("resume")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val session = requireSession(player) ?: return@executes Command.SINGLE_SUCCESS

                        if (!session.isPaused) {
                            sendWarning(player, "Session is already running.")
                            return@executes Command.SINGLE_SUCCESS
                        }
                        session.resume()
                        sendSuccess(player, "Spawn Recorder resumed.")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("stop")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val finished = SpawnRecorderManager.getInstance().stopSession()

                        if (finished == null) {
                            sendWarning(player, "No active session to stop.")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        sendEmptyLine(player)
                        sendSessionSummary(player, "Session Ended", finished, 3)
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("summary")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val session = requireSession(player) ?: return@executes Command.SINGLE_SUCCESS

                        if (session.getTopSpecies(1).isEmpty()) {
                            sendWarning(player, "No spawns recorded yet.")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        sendEmptyLine(player)
                        sendSessionSummary(player, "Session Summary", session, 10)
                        Command.SINGLE_SUCCESS
                    }
            )

    private fun sendSessionSummary(player: LocalPlayer, title: String, session: SpawnRecorderSession, topCount: Int) {
        sendSuccess(player, "Spawn Recorder - $title")
        sendLabeled(player, "  Time elapsed", formatDuration(session.elapsedMs))
        sendLabeled(
            player, "  Spawns", "${session.totalRecordedCount} (${
                formatDecimal(
                    session.spawnsPerMinute.toDouble()
                )
            }/min)"
        )
        sendTopSpecies(player, session.getTopSpecies(topCount))
    }

    private fun sendTopSpecies(player: LocalPlayer, top: List<Map.Entry<String, Int>>) {
        top.forEachIndexed { index, entry ->
            val line = Component.empty()
                .append(Component.literal("    #${index + 1}").withColor(ColorLib.AQUA.rgb))
                .append(Component.literal(" » ").withColor(ColorLib.DARK_GRAY.rgb))
                .append(Component.literal(entry.key).withColor(ColorLib.PINK.rgb))
                .append(Component.literal(" - ").withColor(ColorLib.DARK_GRAY.rgb))
                .append(Component.literal("${entry.value} spawns").withColor(ColorLib.WHITE.rgb))
            sendPrefixed(player, line)
        }
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private fun requireSession(player: LocalPlayer): SpawnRecorderSession? {
        return SpawnRecorderManager.getInstance().session
            ?: run {
                sendWarning(player, "No active session.")
                null
            }
    }
}