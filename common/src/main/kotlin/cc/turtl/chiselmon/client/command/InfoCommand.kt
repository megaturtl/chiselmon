package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.client.util.sendEmptyLine
import cc.turtl.chiselmon.client.util.sendLabeled
import cc.turtl.chiselmon.client.util.sendSuccess
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class InfoCommand : TurtlShellCommand {

    override val name = "info"
    override val description: MutableComponent = Component.literal("Display mod info")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes {
                val player = Minecraft.getInstance().player ?: return@executes 0
                sendEmptyLine(player)
                sendSuccess(player, "${BuildDetails.MOD_DISPLAY_NAME} Info")
                sendLabeled(player, "  Version", BuildDetails.MOD_VERSION)
                sendLabeled(player, "  Author", BuildDetails.MOD_AUTHOR)
                Command.SINGLE_SUCCESS
            }
}