package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.system.alert.AlertManager
import cc.turtl.chiselmon.util.MessageUtils
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.*

class AlertCommand : TurtlShellCommand {

    override val name = "alert"
    override val description: MutableComponent = Component.literal("Manage pokemon alerts")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes { ctx ->
                val player = Minecraft.getInstance().player ?: return@executes 0
                val root = ctx.nodes.first().node.name

                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "Alert Commands")
                MessageUtils.sendPrefixed(player, "  /$root alert muteall - Mutes all currently loaded pokemon")
                MessageUtils.sendPrefixed(
                    player,
                    "  /$root alert unmuteall - Removes all muted pokemon from the current session"
                )
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("mute")
                .then(
                    RequiredArgumentBuilder.argument<CommandSourceStack, String>(
                        "uuid",
                        StringArgumentType.string()
                    )
                        .executes { ctx ->
                            val player = Minecraft.getInstance().player ?: return@executes 0
                            val uuid = UUID.fromString(StringArgumentType.getString(ctx, "uuid"))
                            AlertManager.getInstance().mute(uuid)
                            MessageUtils.sendSuccess(player, "Pokemon muted")
                            Command.SINGLE_SUCCESS
                        }
                )
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("muteall")
                    .executes { ctx ->
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        AlertManager.getInstance().muteAll()
                        MessageUtils.sendSuccess(player, "All loaded pokemon muted")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("unmuteall")
                    .executes { ctx ->
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        AlertManager.getInstance().unmuteAll()
                        MessageUtils.sendSuccess(player, "All loaded pokemon unmuted")
                        Command.SINGLE_SUCCESS
                    }
            )
}