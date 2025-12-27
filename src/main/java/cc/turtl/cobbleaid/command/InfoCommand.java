package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import cc.turtl.cobbleaid.util.CommandUtils;

public class InfoCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("info")
                .executes(InfoCommand::execute);
    }
    private static int execute(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Cobble Aid Info");
        CommandUtils.sendLabeled(source, "Version", CobbleAid.VERSION);
        CommandUtils.sendLabeled(source, "Author", "megaturtl");
        return 1;
    }
    
}