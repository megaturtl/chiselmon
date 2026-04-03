package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.ChiselmonConstants
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates
import cc.turtl.chiselmon.util.MessageUtils
import cc.turtl.chiselmon.util.ObjectDumper
import cc.turtl.chiselmon.util.format.PokemonFormats
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

                MessageUtils.sendEmptyLine(player)
                MessageUtils.sendSuccess(player, "Debug Commands")
                MessageUtils.sendPrefixed(player, "  /$root debug test")
                MessageUtils.sendPrefixed(player, "  /$root debug dumpentity")
                Command.SINGLE_SUCCESS
            }
            .then(
                LiteralArgumentBuilder.literal<CommandSourceStack>("test")
                    .executes {
                        val player = Minecraft.getInstance().player ?: return@executes 0
                        MessageUtils.sendSuccess(player, "Test successful!")
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
                                MessageUtils.sendWarning(player, "Not looking at an entity!")
                                return@executes Command.SINGLE_SUCCESS
                            }

                            if (target is PokemonEntity) {
                                MessageUtils.sendEmptyLine(player)
                                MessageUtils.sendPrefixed(player, PokemonFormats.detailedName(target.pokemon, false))
                                MessageUtils.sendLabeled(player, "  NoAI", target.isNoAi)
                                MessageUtils.sendLabeled(player, "  Busy", target.isBusy)
                                MessageUtils.sendLabeled(
                                    player,
                                    "  Owned",
                                    PokemonEntityPredicates.IS_OWNED.test(target)
                                )
                                MessageUtils.sendLabeled(player, "  Wild", PokemonEntityPredicates.IS_WILD.test(target))
                            } else {
                                MessageUtils.sendWarning(player, "Entity is a ${target.type.description.string}")
                            }
                            ObjectDumper.dump(ChiselmonConstants.LOGGER, target)
                            Command.SINGLE_SUCCESS
                        } catch (e: Exception) {
                            MessageUtils.sendError(player, e)
                            0
                        }
                    }
            )
}