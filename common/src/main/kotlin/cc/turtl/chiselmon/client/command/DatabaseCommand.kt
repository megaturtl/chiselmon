package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.api.storage.StorageScope
import cc.turtl.chiselmon.system.tracker.TrackerManager
import cc.turtl.chiselmon.util.MessageUtils
import cc.turtl.chiselmon.util.format.StringFormats
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
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

                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "DB Info for ${StorageScope.currentWorld()!!.worldKey}")
                MessageUtils.sendLabeled(player, "  Encounters in write cache", db.writeCachedCount)
                MessageUtils.sendLabeled(player, "  Encounters stored on disk", encounters)
                MessageUtils.sendLabeled(player, "    Legendaries", legendaries)
                MessageUtils.sendLabeled(player, "    Shinies", shinies)
                MessageUtils.sendLabeled(
                    player,
                    "  Database size on disk",
                    StringFormats.formatBytes(db.sizeOnDiskBytes)
                )
                Command.SINGLE_SUCCESS
            }
}