package cc.turtl.chiselmon.feature.spawnalert;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import java.util.UUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.module.feature.SpawnAlertModule;
import cc.turtl.chiselmon.util.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SpawnAlertCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("alert")
                .executes(SpawnAlertCommand::executeHelp)
                .then(literal("mute")
                        .then(argument("uuid", StringArgumentType.string())
                                .executes(SpawnAlertCommand::executeMute)))
                .then(literal("muteall")
                        .executes(SpawnAlertCommand::executeMuteAll))
                .then(literal("unmuteall")
                        .executes(SpawnAlertCommand::executeUnmuteAll));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String modid = ChiselmonConstants.MODID;

        CommandUtils.sendHeader(source, "Spawn Alert Commands");
        CommandUtils.sendUsage(source, "/" + modid + " alert mute <UUID>");
        CommandUtils.sendUsage(source, "/" + modid + " alert muteall");
        CommandUtils.sendUsage(source, "/" + modid + " alert unmuteall");
        return 1;
    }

    private static int executeMute(CommandContext<FabricClientCommandSource> context) {
        return CommandUtils.executeWithErrorHandling(context, source -> {
            UUID uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"));
            getManager().mute(uuid);
            CommandUtils.sendSuccess(source, "Pokemon muted.");
        });
    }

    private static int executeMuteAll(CommandContext<FabricClientCommandSource> context) {
        return CommandUtils.executeWithErrorHandling(context, source -> {
            getManager().muteAll();
            CommandUtils.sendSuccess(source, "All active alerts muted.");
        });
    }

    private static int executeUnmuteAll(CommandContext<FabricClientCommandSource> context) {
        return CommandUtils.executeWithErrorHandling(context, source -> {
            getManager().unmuteAll();
            CommandUtils.sendSuccess(source, "All alerts unmuted.");
        });
    }

    private static AlertManager getManager() {
        return SpawnAlertModuleHolder.get().getAlertManager();
    }

    private static final class SpawnAlertModuleHolder {
        private static volatile SpawnAlertFeature moduleFeature;

        private static SpawnAlertFeature get() {
            SpawnAlertFeature cached = moduleFeature;
            if (cached != null) {
                return cached;
            }
            synchronized (SpawnAlertModuleHolder.class) {
                if (moduleFeature == null) {
                    SpawnAlertModule module = cc.turtl.chiselmon.Chiselmon.modules()
                            .getModule(SpawnAlertModule.class);
                    if (module == null) {
                        throw new IllegalStateException("Spawn alert module is not registered");
                    }
                    moduleFeature = module.feature();
                }
                return moduleFeature;
            }
        }
    }
}
