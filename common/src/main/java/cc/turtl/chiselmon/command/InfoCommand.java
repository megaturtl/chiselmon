package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.CommandUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class InfoCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Display mod info";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CommandUtils.sendHeader(source, ChiselmonConstants.MOD_NAME + " Info");
        CommandUtils.sendLabeled(source, "Version", ChiselmonConstants.VERSION);
        CommandUtils.sendLabeled(source, "Author", ChiselmonConstants.AUTHOR);
        return Command.SINGLE_SUCCESS;
    }
}