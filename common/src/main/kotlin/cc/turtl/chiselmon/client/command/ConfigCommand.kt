package cc.turtl.chiselmon.client.command

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.turtlshell.api.core.command.TurtlShellCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class ConfigCommand : TurtlShellCommand {

    override val name = "config"
    override val description: MutableComponent = Component.literal("Open the mod config screen")

    override fun build(): LiteralArgumentBuilder<CommandSourceStack> =
        LiteralArgumentBuilder.literal<CommandSourceStack>(name)
            .executes {
                openScreen(0)
                Command.SINGLE_SUCCESS
            }
            .then(
                RequiredArgumentBuilder.argument<CommandSourceStack, String>("tab", StringArgumentType.word())
                    .suggests { _, builder ->
                        TABS.forEach(builder::suggest)
                        builder.buildFuture()
                    }
                    .executes { ctx ->
                        val tab = StringArgumentType.getString(ctx, "tab")
                        openScreen(maxOf(TABS.indexOf(tab), 0))
                        Command.SINGLE_SUCCESS
                    }
            )

    private fun openScreen(tabIndex: Int) {
        val mc = Minecraft.getInstance()
        mc.tell { ChiselmonConfig.openAtTab(mc.screen, tabIndex) }
    }

    companion object {
        private val TABS = listOf("general", "pc", "filter", "alert", "recorder")
    }
}