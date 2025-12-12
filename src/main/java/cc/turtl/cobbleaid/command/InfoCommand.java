package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class InfoCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("info")
                .executes(InfoCommand::execute);
    }
    private static int execute(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandFeedbackHelper.sendHeader(source, "Cobble Aid Info");
        CommandFeedbackHelper.sendInfo(source, "Version", CobbleAid.VERSION);
        CommandFeedbackHelper.sendInfo(source, "Author", "megaturtl");
        return 1;
    }
    
}
