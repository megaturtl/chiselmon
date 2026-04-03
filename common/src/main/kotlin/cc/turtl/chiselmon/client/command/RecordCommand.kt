package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderSession
import cc.turtl.chiselmon.util.MessageUtils
import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.chiselmon.util.format.StringFormats
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
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

                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "Spawn Recorder - Commands")
                MessageUtils.sendPrefixed(player, "  /$root record start")
                MessageUtils.sendPrefixed(player, "  /$root record pause")
                MessageUtils.sendPrefixed(player, "  /$root record resume")
                MessageUtils.sendPrefixed(player, "  /$root record stop")
                MessageUtils.sendPrefixed(player, "  /$root record summary")
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("start")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        try {
                            val started = SpawnRecorderManager.getInstance().startSession()
                            if (!started) {
                                MessageUtils.sendWarning(player, "A Spawn Recorder session is already running!")
                            } else {
                                MessageUtils.sendSuccess(player, "Spawn Recorder session started!")
                            }
                        } catch (e: Exception) {
                            MessageUtils.sendError(player, e)
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
                            MessageUtils.sendWarning(player, "Session is already paused.")
                            return@executes Command.SINGLE_SUCCESS
                        }
                        session.pause()
                        MessageUtils.sendSuccess(player, "Spawn Recorder paused.")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("resume")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val session = requireSession(player) ?: return@executes Command.SINGLE_SUCCESS

                        if (!session.isPaused) {
                            MessageUtils.sendWarning(player, "Session is already running.")
                            return@executes Command.SINGLE_SUCCESS
                        }
                        session.resume()
                        MessageUtils.sendSuccess(player, "Spawn Recorder resumed.")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("stop")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        val finished = SpawnRecorderManager.getInstance().stopSession()

                        if (finished == null) {
                            MessageUtils.sendWarning(player, "No active session to stop.")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        MessageUtils.sendEmptyLine(player)
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
                            MessageUtils.sendWarning(player, "No spawns recorded yet.")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        MessageUtils.sendEmptyLine(player)
                        sendSessionSummary(player, "Session Summary", session, 10)
                        Command.SINGLE_SUCCESS
                    }
            )

    private fun sendSessionSummary(player: LocalPlayer, title: String, session: SpawnRecorderSession, topCount: Int) {
        MessageUtils.sendSuccess(player, "Spawn Recorder - $title")
        MessageUtils.sendLabeled(player, "  Time elapsed", StringFormats.formatDurationMs(session.elapsedMs))
        MessageUtils.sendLabeled(
            player, "  Spawns", "${session.totalRecordedCount} (${
                StringFormats.formatDecimal(
                    session.spawnsPerMinute.toDouble()
                )
            }/min)"
        )
        sendTopSpecies(player, session.getTopSpecies(topCount))
    }

    private fun sendTopSpecies(player: LocalPlayer, top: List<Map.Entry<String, Int>>) {
        top.forEachIndexed { index, entry ->
            val line = Component.empty()
                .append(Component.literal("    #${index + 1}").withColor(ColorUtils.AQUA.rgb))
                .append(Component.literal(" » ").withColor(ColorUtils.DARK_GRAY.rgb))
                .append(Component.literal(entry.key).withColor(ColorUtils.PINK.rgb))
                .append(Component.literal(" - ").withColor(ColorUtils.DARK_GRAY.rgb))
                .append(Component.literal("${entry.value} spawns").withColor(ColorUtils.WHITE.rgb))
            MessageUtils.sendPrefixed(player, line)
        }
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private fun requireSession(player: LocalPlayer): SpawnRecorderSession? {
        return SpawnRecorderManager.getInstance().session
            ?: run {
                MessageUtils.sendWarning(player, "No active session.")
                null
            }
    }
}