package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.client.util.sendEmptyLine
import cc.turtl.chiselmon.client.util.sendLabeled
import cc.turtl.chiselmon.client.util.sendSuccess
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.chiselmon.system.tracker.TrackerManager
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import cc.turtl.turtlshell.api.core.format.formatBytes
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.sql.SQLException

class DatabaseCommand : TurtlShellCommand {

    override val name = "db"
    override val description: MutableComponent = Component.literal("Manage the DB for the current world")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes {
                val player = Minecraft.getInstance().player ?: return@executes 0
                val db = TrackerManager.getInstance().tracker.db

                var encounters: String
                var legendaries: String
                var shinies: String
                try {
                    encounters = db.savedEncounters.toString()
                    legendaries = db.legendaryCount.toString()
                    shinies = db.shinyCount.toString()
                } catch (e: SQLException) {
                    encounters = "ERROR"
                    legendaries = "ERROR"
                    shinies = "ERROR"
                }

                sendEmptyLine(player)
                sendSuccess(player, "DB Info for ${Scope.currentWorld()?.key}")
                sendLabeled(player, "  Encounters in write cache", db.writeCachedCount)
                sendLabeled(player, "  Encounters stored on disk", encounters)
                sendLabeled(player, "    Legendaries", legendaries)
                sendLabeled(player, "    Shinies", shinies)
                sendLabeled(
                    player,
                    "  Database size on disk",
                    formatBytes(db.sizeOnDiskBytes)
                )
                Command.SINGLE_SUCCESS
            }
}