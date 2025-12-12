package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.WorldDataManager;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.ComponentFormatUtil;
import cc.turtl.cobbleaid.util.ObjectDumper;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class DebugCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("debug")
                .executes(DebugCommand::executeHelp)
                .then(literal("test")
                        .executes(DebugCommand::executeTest))
                .then(literal("dump")
                        .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes(DebugCommand::executeDump))
                        .then(literal("look").executes(DebugCommand::executeLookDump)));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandFeedbackHelper.sendHeader(source, "Debug Commands");
        CommandFeedbackHelper.sendUsageWithDescription(source, "/" + CobbleAid.MODID + " debug dump <slot>",
                "Dumps info about provided slot.");
        CommandFeedbackHelper.sendUsageWithDescription(source, "/" + CobbleAid.MODID + " debug dump look",
                "Dumps info about targeted pokemon entity.");
        return 1;
    }

    private static int executeTest(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(ComponentFormatUtil.colored("Testing random things", ColorUtil.CYAN));
        context.getSource().sendFeedback(ComponentFormatUtil.colored("World ID:", ColorUtil.LIGHT_GRAY));
        context.getSource()
                .sendFeedback(ComponentFormatUtil.colored(WorldDataManager.getWorldIdentifier(), ColorUtil.WHITE));
        return 1;
    }

    private static int executeDump(CommandContext<FabricClientCommandSource> context) {
        CobbleAidLogger LOGGER = CobbleAid.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            int slot = IntegerArgumentType.getInteger(context, "slot") - 1;

            CobblemonClient client = CobblemonClient.INSTANCE;
            ClientStorageManager storageManager = client.getStorage();
            ClientParty party = storageManager.getParty();
            Pokemon pokemon = party.get(slot);

            if (pokemon == null) {
                CommandFeedbackHelper.sendError(source, "Party slot " + (slot + 1) + " is empty.");
                return 1;
            }

            source.sendFeedback(
                    ComponentFormatUtil.colored("--- Dumping Pokemon at Slot " + (slot + 1) + " ---", ColorUtil.CYAN));
            CommandFeedbackHelper.sendLabeled(source, "Species", pokemon.getSpecies().getName());
            CommandFeedbackHelper.sendLabeled(source, "Species Resource ID",
                    pokemon.getSpecies().getResourceIdentifier().toString());
            CommandFeedbackHelper.sendLabeled(source, "Level", String.valueOf(pokemon.getLevel()));

            LOGGER.info("--- DUMPING FIELDS FOR POKEMON '{}' at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            ObjectDumper.logObjectFields(LOGGER, pokemon);

            CommandFeedbackHelper.sendWarning(source, "Full object dump sent to console/log.");

            return 1;

        } catch (Exception e) {
            CommandFeedbackHelper.sendError(source, "An unexpected error occurred during dump command!");
            CobbleAid.getLogger().error("Error executing dump command:", e);
            return 0;
        }
    }

    private static int executeLookDump(CommandContext<FabricClientCommandSource> context) {
        CobbleAidLogger LOGGER = CobbleAid.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            source.sendFeedback(ComponentFormatUtil.colored("--- Dumping Targeted Entity ---", ColorUtil.CYAN));
            Minecraft minecraftClient = Minecraft.getInstance();
            Entity lookingAtEntity = minecraftClient.crosshairPickEntity;

            if (lookingAtEntity == null) {
                CommandFeedbackHelper.sendError(source, "Not looking at a pokemon!");
                return 0;
            }

            if (lookingAtEntity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();

                source.sendFeedback(
                        ComponentFormatUtil.colored("--- Dumping Targeted POKEMON Object ---", ColorUtil.GREEN));
                CommandFeedbackHelper.sendLabeled(source, "Moves", pokemonEntity.getPokemon().getMoveSet().getMoves()
                        .stream().map(m -> m.getTemplate().getName()).toList().toString());
                LOGGER.info("Logging object details for targeted Pokemon...");
                ObjectDumper.logObjectFields(LOGGER, pokemon);
                CommandFeedbackHelper.sendWarning(source, "Full object dump sent to console/log.");
                return 1;

            } else {
                CommandFeedbackHelper.sendError(source, "Targeted entity is a "
                        + lookingAtEntity.getClass().getSimpleName() + ", not a Pokémon.");
                return 0;
            }

        } catch (Exception e) {
            CommandFeedbackHelper.sendError(source, "An unexpected error occurred during look dump command!");
            LOGGER.error("Error executing dump look command:", e);
            return 0;
        }
    }
}