package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.util.MessageUtils;
import cc.turtl.chiselmon.util.ObjectDumper;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;

public class DebugCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return "Debug utilities";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::showHelp)
                .then(Commands.literal("test")
                        .executes(this::executeTest))
                .then(Commands.literal("dumpentity")
                        .executes(this::executeDumpEntity));
    }

    private int showHelp(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        String root = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendHeader(player, "Debug Commands");
        MessageUtils.sendPrefixed(player, "/" + root + " debug test - Test command");
        MessageUtils.sendPrefixed(player, "/" + root + " debug dumpentity - Dumps targeted entity info to console w/ summary in game");
        return Command.SINGLE_SUCCESS;
    }

    private int executeTest(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        MessageUtils.sendSuccess(player, "Test successful!");
        return Command.SINGLE_SUCCESS;
    }

    private int executeDumpEntity(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Entity target = minecraft.crosshairPickEntity;

            if (target == null) {
                MessageUtils.sendWarning(player, "Not looking at an entity!");
                return Command.SINGLE_SUCCESS;
            }

            if (target instanceof PokemonEntity pe) {
                MessageUtils.sendHeader(player, PokemonFormats.detailedName(pe.getPokemon()));
                MessageUtils.sendLabeled(player, "NoAI", pe.isNoAi());
                MessageUtils.sendLabeled(player, "Busy", pe.isBusy());
                MessageUtils.sendLabeled(player, "Owned", PokemonEntityPredicates.IS_OWNED.test(pe));
                MessageUtils.sendLabeled(player, "Wild", PokemonEntityPredicates.IS_WILD.test(pe));
            } else {
                MessageUtils.sendWarning(player, "Entity is a " + target.getType().getDescription().getString());
            }
            ObjectDumper.dump(ChiselmonConstants.LOGGER, target);
            return Command.SINGLE_SUCCESS;

        } catch (Exception e) {
            MessageUtils.sendError(player, context, e);
            return 0;
        }
    }
}