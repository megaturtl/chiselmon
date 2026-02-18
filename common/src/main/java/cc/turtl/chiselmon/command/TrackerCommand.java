package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.CommandUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
        TrackerSession tracker = TrackerManager.getInstance().getTracker();
        CommandSourceStack source = context.getSource();
        CommandUtils.sendHeader(source, ChiselmonConstants.MOD_NAME + " Tracker");
        CommandUtils.sendLabeled(source, "Loaded pokemon count", tracker.getCurrentlyLoaded().size());
        CommandUtils.sendLabeled(source, "Unique encounters this session", tracker.getEncounterCount());
        CommandUtils.sendLabeled(source, "Time elapsed this session", StringFormats.formatDurationMs(tracker.getMsElapsed()));

        return Command.SINGLE_SUCCESS;
    }
}