package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.system.alert.AlertManager;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.UUID;

public class AlertCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "alert";
    }

    @Override
    public String getDescription() {
        return "Manage pokemon alerts";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::showHelp)
                .then(Commands.literal("mute")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(this::executeMute)))
                .then(Commands.literal("muteall")
                        .executes(this::executeMuteAll))
                .then(Commands.literal("unmuteall")
                        .executes(this::executeUnmuteAll));
    }

    private int showHelp(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        String root = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendHeader(player, "Alert Commands");
        MessageUtils.sendPrefixed(player, "/" + root + " alert muteall - Mutes all currently loaded pokemon");
        MessageUtils.sendPrefixed(player, "/" + root + " alert unmuteall - Removes all muted pokemon from the current session");
        return Command.SINGLE_SUCCESS;
    }

    private int executeMute(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        AlertManager alerter = AlertManager.getInstance();

        UUID uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"));
        alerter.mute(uuid);
        MessageUtils.sendSuccess(player, "Pokemon muted");
        return Command.SINGLE_SUCCESS;
    }

    private int executeMuteAll(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        AlertManager alerter = AlertManager.getInstance();
        alerter.muteAll();
        MessageUtils.sendSuccess(player, "All loaded pokemon muted");
        return Command.SINGLE_SUCCESS;
    }

    private int executeUnmuteAll(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        AlertManager alerter = AlertManager.getInstance();
        alerter.unmuteAll();
        MessageUtils.sendSuccess(player, "All loaded pokemon unmuted");
        return Command.SINGLE_SUCCESS;
    }
}