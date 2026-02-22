package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.api.storage.StorageScope;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.MessageUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.sql.SQLException;
import java.util.Objects;

public class DatabaseCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "db";
    }

    @Override
    public String getDescription() {
        return "Display info about the Chiselmon Database for the current world.";
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
        EncounterDatabase db = tracker.getDb();

        String encounters, legendaries, shinies;
        try {
            encounters = String.valueOf(db.getSavedEncounters());
            legendaries = String.valueOf(db.getLegendaryCount());
            shinies = String.valueOf(db.getShinyCount());
        } catch (SQLException e) {
            encounters = legendaries = shinies = "ERROR";
        }

        MessageUtils.sendHeader(player, "Database Info for " + Objects.requireNonNull(StorageScope.currentWorld()).getWorldKey());
        MessageUtils.sendLabeled(player, "Encounters in write cache", db.getWriteCachedCount());
        MessageUtils.sendLabeled(player, "Encounters stored on disk", encounters);
        MessageUtils.sendLabeled(player, "  Legendaries", legendaries);
        MessageUtils.sendLabeled(player, "  Shinies", shinies);
        MessageUtils.sendLabeled(player, "Database size on disk", StringFormats.formatBytes(db.getSizeOnDiskBytes()));

        return Command.SINGLE_SUCCESS;
    }
}