package cc.turtl.chiselmon.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class InfoCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("info")
                .executes(InfoCommand::execute);
    }
    private static int execute(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, ChiselmonConstants.MODNAME + " Info");
        CommandUtils.sendLabeled(source, "Version", Chiselmon.VERSION);
        CommandUtils.sendLabeled(source, "Author", "megaturtl");
        return 1;
    }
    
}