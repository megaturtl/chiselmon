package cc.turtl.chiselmon.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import org.apache.logging.log4j.Logger;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.SimpleSpecies;
import cc.turtl.chiselmon.api.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.CommandUtils;
import cc.turtl.chiselmon.util.ComponentFormatUtil;
import cc.turtl.chiselmon.util.ObjectDumper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class DebugCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        var speciesBranch = literal("species")
                .then(argument("name", StringArgumentType.word())
                        .executes(DebugCommand::executeSpeciesDump));

        var dumpBranch = literal("dump")
                .then(argument("slot", IntegerArgumentType.integer(1, 6))
                        .executes(DebugCommand::executeDump))
                .then(literal("look")
                        .executes(DebugCommand::executeLookDump));

        return literal("debug")
                .executes(DebugCommand::executeHelp)
                .then(literal("test").executes(DebugCommand::executeTest))
                .then(dumpBranch)
                .then(speciesBranch);
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Debug Commands");
        CommandUtils.sendUsageWithDescription(source, "/" + Chiselmon.MODID + " debug dump <slot>",
                "Dumps info about provided slot.");
        CommandUtils.sendUsageWithDescription(source, "/" + Chiselmon.MODID + " debug dump look",
                "Dumps info about targeted pokemon entity.");
        return 1;
    }

    private static int executeTest(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(ComponentFormatUtil.colored("Testing random things", ColorUtil.AQUA));
        return 1;
    }

    private static int executeDump(CommandContext<FabricClientCommandSource> context) {
        Logger LOGGER = Chiselmon.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            int slot = IntegerArgumentType.getInteger(context, "slot") - 1;

            CobblemonClient client = CobblemonClient.INSTANCE;
            ClientStorageManager storageManager = client.getStorage();
            ClientParty party = storageManager.getParty();
            Pokemon pokemon = party.get(slot);

            if (pokemon == null) {
                CommandUtils.sendError(source, "Party slot " + (slot + 1) + " is empty.");
                return 1;
            }

            source.sendFeedback(
                    ComponentFormatUtil.colored("--- Dumping Pokemon at Slot " + (slot + 1) + " ---", ColorUtil.AQUA));
            CommandUtils.sendLabeled(source, "Species", pokemon.getSpecies().getName());
            CommandUtils.sendLabeled(source, "Species Resource ID",
                    pokemon.getSpecies().getResourceIdentifier().toString());
            CommandUtils.sendLabeled(source, "Level", String.valueOf(pokemon.getLevel()));

            LOGGER.info("--- DUMPING FIELDS FOR POKEMON '{}' at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            ObjectDumper.logObjectFields(LOGGER, pokemon);

            CommandUtils.sendWarning(source, "Full object dump sent to console/log.");

            return 1;

        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during dump command!");
            Chiselmon.getLogger().error("Error executing dump command:", e);
            return 0;
        }
    }

    private static int executeLookDump(CommandContext<FabricClientCommandSource> context) {
        Logger LOGGER = Chiselmon.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            source.sendFeedback(ComponentFormatUtil.colored("--- Dumping Targeted Entity ---", ColorUtil.AQUA));
            Minecraft minecraftClient = Minecraft.getInstance();
            Entity lookingAtEntity = minecraftClient.crosshairPickEntity;

            if (lookingAtEntity == null) {
                CommandUtils.sendError(source, "Not looking at a pokemon!");
                return 0;
            }

            if (lookingAtEntity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();

                source.sendFeedback(
                        ComponentFormatUtil.colored("--- Dumping Targeted POKEMON Object ---", ColorUtil.GREEN));
                CommandUtils.sendLabeled(source, "Moves", pokemonEntity.getPokemon().getMoveSet().getMoves()
                        .stream().map(m -> m.getTemplate().getName()).toList().toString());
                LOGGER.info("Logging object details for targeted Pokemon...");
                ObjectDumper.logObjectFields(LOGGER, pokemon);
                CommandUtils.sendWarning(source, "Full object dump sent to console/log.");
                return 1;

            } else {
                CommandUtils.sendError(source, "Targeted entity is a "
                        + lookingAtEntity.getClass().getSimpleName() + ", not a Pokemon.");
                return 0;
            }

        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during look dump command!");
            LOGGER.error("Error executing dump look command:", e);
            return 0;
        }
    }

    private static int executeSpeciesDump(CommandContext<FabricClientCommandSource> context) {
        Logger LOGGER = Chiselmon.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            String speciesName = StringArgumentType.getString(context, "name");

            SimpleSpecies species = SimpleSpeciesRegistry.getByName(speciesName);

            source.sendFeedback(
                    ComponentFormatUtil.colored("--- Dumping Species " + species.name + " ---", ColorUtil.AQUA));
            CommandUtils.sendLabeled(source, "Catch Rate", species.catchRate);
            CommandUtils.sendLabeled(source, "Egg Groups", species.eggGroups);
            CommandUtils.sendLabeled(source, "EV Yield", species.evYield);

            LOGGER.info("--- DUMPING FIELDS FOR SPECIES '{}' ---", species.name);
            ObjectDumper.logObjectFields(LOGGER, species);

            CommandUtils.sendWarning(source, "Full species object dump sent to console/log.");

            return 1;

        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during species dump command!");
            Chiselmon.getLogger().error("Error executing dump command:", e);
            return 0;
        }
    }
}