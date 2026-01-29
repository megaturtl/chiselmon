package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.util.CommandUtils;
import cc.turtl.chiselmon.util.ObjectDumper;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
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
        CommandSourceStack source = context.getSource();
        String root = context.getNodes().getFirst().getNode().getName();

        CommandUtils.sendHeader(source, "Debug Commands");
        CommandUtils.sendPrefixed(source, "/" + root + " debug test - Test command");
        CommandUtils.sendPrefixed(source, "/" + root + " debug dumpentity - Dumps targeted entity info to console w/ summary in game");
        return Command.SINGLE_SUCCESS;
    }

    private int executeTest(CommandContext<CommandSourceStack> context) {
        CommandUtils.sendSuccess(context.getSource(), "Test successful!");
        return Command.SINGLE_SUCCESS;
    }

    private int executeDumpEntity(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Entity target = minecraft.crosshairPickEntity;

            if (target == null) {
                CommandUtils.sendWarning(source, "Not looking at an entity!");
                return Command.SINGLE_SUCCESS;
            }

            if (target instanceof PokemonEntity pe) {
                CommandUtils.sendHeader(source, PokemonFormats.detailedName(pe.getPokemon()));
                CommandUtils.sendLabeled(source, "NoAI", pe.isNoAi());
                CommandUtils.sendLabeled(source, "Busy", pe.isBusy());
                CommandUtils.sendLabeled(source, "Owned", PokemonEntityPredicates.IS_OWNED.test(pe));
                CommandUtils.sendLabeled(source, "Wild", PokemonEntityPredicates.IS_WILD.test(pe));
            } else {
                CommandUtils.sendWarning(source, "Entity is a " + target.getType().getDescription().getString());
            }
            ObjectDumper.dump(ChiselmonConstants.LOGGER, target);
            return Command.SINGLE_SUCCESS;

        } catch (Exception e) {
            CommandUtils.sendError(context, e);
            return 0;
        }
    }
}