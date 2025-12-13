package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.ComponentFormatUtil;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ConfigCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("config")
                .executes(ConfigCommand::executeHelp)
                .then(literal("enable").executes(ConfigCommand::executeEnable))
                .then(literal("disable").executes(ConfigCommand::executeDisable))
                .then(literal("status").executes(ConfigCommand::executeStatus));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandFeedbackHelper.sendHeader(source, "Config Commands");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " config enable");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " config disable");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " config status");
        return 1;
    }

    private static int executeEnable(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.services().config().get();
        config.modDisabled = false;
        CobbleAid.services().config().save();

        CommandFeedbackHelper.sendSuccess(context.getSource(), "Cobble Aid has been enabled!");
        return 1;
    }

    private static int executeDisable(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.services().config().get();
        config.modDisabled = true;
        CobbleAid.services().config().save();

        CommandFeedbackHelper.sendError(context.getSource(), "Cobble Aid has been disabled!");
        return 1;
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.services().config().get();
        FabricClientCommandSource source = context.getSource();

        if (config.modDisabled) {
            CommandFeedbackHelper.sendLabeled(source, "Cobble Aid Status",
                    ComponentFormatUtil.colored("Disabled", ColorUtil.RED));
        } else {
            CommandFeedbackHelper.sendLabeled(source, "Cobble Aid Status",
                    ComponentFormatUtil.colored("Enabled", ColorUtil.GREEN));
        }

        return 1;
    }
}
