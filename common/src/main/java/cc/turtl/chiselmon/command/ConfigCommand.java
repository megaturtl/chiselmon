package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "command";
    }

    @Override
    public String getDescription() {
        return "Open the mod config screen";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        ChiselmonConfig.createScreen(Minecraft.getInstance().screen);
        return Command.SINGLE_SUCCESS;
    }
}