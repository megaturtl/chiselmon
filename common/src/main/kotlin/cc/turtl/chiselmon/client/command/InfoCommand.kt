package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.ChiselmonConstants
import cc.turtl.chiselmon.util.MessageUtils
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
                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "${ChiselmonConstants.MOD_DISPLAY_NAME} Info")
                MessageUtils.sendLabeled(player, "  Version", ChiselmonConstants.VERSION)
                MessageUtils.sendLabeled(player, "  Author", ChiselmonConstants.AUTHOR)
                Command.SINGLE_SUCCESS
            }
}