package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.core.api.predicate.IS_OWNED
import cc.turtl.chiselmon.core.api.predicate.IS_WILD
import cc.turtl.chiselmon.client.util.*
import cc.turtl.chiselmon.core.util.format.PokemonFormats
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class DebugCommand : TurtlShellCommand {

    override val name = "debug"
    override val description: MutableComponent = Component.literal("Debug utilities")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes { ctx ->
                val player = Minecraft.getInstance().player ?: return@executes 0
                val root = ctx.nodes.first().node.name

                sendEmptyLine(player)
                sendSuccess(player, "Debug Commands")
                sendPrefixed(player, "  /$root debug test")
                sendPrefixed(player, "  /$root debug dumpentity")
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("test")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        sendSuccess(player, "Test successful!")
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("dumpentity")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        try {
                            val target = Minecraft.getInstance().crosshairPickEntity
                            if (target == null) {
                                sendWarning(player, "Not looking at an entity!")
                                return@executes Command.SINGLE_SUCCESS
                            }

                            if (target is PokemonEntity) {
                                sendEmptyLine(player)
                                sendPrefixed(player, PokemonFormats.detailedName(target.pokemon, false))
                                sendLabeled(player, "  NoAI", target.isNoAi)
                                sendLabeled(player, "  Busy", target.isBusy)
                                sendLabeled(
                                    player,
                                    "  Owned",
                                    IS_OWNED.test(target)
                                )
                                sendLabeled(player, "  Wild", IS_WILD.test(target))
                            } else {
                                sendWarning(player, "Entity is a ${target.type.description.string}")
                            }
                            Command.SINGLE_SUCCESS
                        } catch (e: Exception) {
                            sendError(player, e)
                            0
                        }
                    }
            )
}