package cc.turtl.cobbleaid.feature.spawnalert;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import java.util.UUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.util.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SpawnAlertCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("alert")
                .executes(SpawnAlertCommand::executeHelp)
                .then(literal("mute").then(argument("uuid", StringArgumentType.string())
                        .executes(SpawnAlertCommand::executeMute)))
                .then(literal("unmuteall")
                        .executes(SpawnAlertCommand::executeUnmuteAll));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Spawn Alert Commands");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " alert mute <UUID>");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " alert unmuteall");
        return 1;
    }

    private static int executeMute(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        try {
            UUID uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"));
            SpawnAlertFeature.getInstance().getAlertManager().muteTarget(uuid);
            CommandUtils.sendSuccess(source, "Pokemon muted.");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred!");
            CobbleAid.getLogger().error("Error executing 'alert mute' command:", e);
        }
        return 1;
    }

    private static int executeUnmuteAll(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        try {
            SpawnAlertFeature.getInstance().getAlertManager().unmuteAllTargets();
            CommandUtils.sendSuccess(source, "All alerts unmuted.");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred!");
            CobbleAid.getLogger().error("Error executing 'alert unmuteall' command:", e);
        }
        return 1;
    }

}
