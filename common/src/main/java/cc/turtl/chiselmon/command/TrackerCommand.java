package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TrackerCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "tracker";
    }

    @Override
    public String getDescription() {
        return "Display info about currently tracked pokemon";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        TrackerSession tracker = TrackerManager.getInstance().getTracker();

        MessageUtils.sendHeader(player, ChiselmonConstants.MOD_NAME + " Wild Pokemon Tracker");
        MessageUtils.sendLabeled(player, "Currently loaded", tracker.getCurrentlyLoaded().size());
        MessageUtils.sendLabeled(player, "Encounters since join", tracker.getEncounterCount());

        return Command.SINGLE_SUCCESS;
    }
}