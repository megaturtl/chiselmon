package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;


import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.CobbleAidLogger;

import com.cobblemon.mod.common.api.riding.RidingProperties;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import cc.turtl.cobbleaid.util.ObjectDumper;

public class DebugCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("debug")
                .executes(DebugCommand::executeHelp)
                .then(literal("dump")
                        .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes(DebugCommand::executeDump)))
                .then(literal("ridedump")
                        .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes(DebugCommand::executeRideDump)));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§d=== Debug Commands ==="));
        context.getSource()
                .sendFeedback(Component.literal("§7/cobbleaid debug dump <slot> §f- Dumps info about provided slot."));
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
                source.sendFeedback(Component.literal("§c[Cobble Aid] Party slot " + (slot + 1) + " is empty."));
                return 1;
            }

            source.sendFeedback(Component.literal("--- Dumping Pokemon at Slot " + (slot + 1) + " ---")
                    .withStyle(s -> s.withColor(TextColor.fromRgb(0x55FFFF))));
            source.sendFeedback(Component.literal("Species: §f" + pokemon.getSpecies().getName()));
            source.sendFeedback(Component.literal("Species Resource ID: §f" + pokemon.getSpecies().getResourceIdentifier()));
            source.sendFeedback(Component.literal("Level: §f" + pokemon.getLevel()));

            LOGGER.info("--- DUMPING FIELDS FOR POKEMON '{}' at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            ObjectDumper.logObjectFields(LOGGER, pokemon);

            source.sendFeedback(Component.literal("§eFull object dump sent to console/log."));

            return 1;

        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobble Aid] An unexpected error occurred during dump command!"));
            LOGGER.error("Error executing dump command:", e);
            return 0;
        }
    }

    private static int executeRideDump(CommandContext<FabricClientCommandSource> context) {
        CobbleAidLogger LOGGER = CobbleAid.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            int slot = IntegerArgumentType.getInteger(context, "slot") - 1;

            CobblemonClient client = CobblemonClient.INSTANCE;
            ClientStorageManager storageManager = client.getStorage();
            ClientParty party = storageManager.getParty();
            Pokemon pokemon = party.get(slot);

            if (pokemon == null) {
                source.sendFeedback(Component.literal("§c[Cobble Aid] Party slot " + (slot + 1) + " is empty."));
                return 1;
            }

            source.sendFeedback(Component.literal("--- Dumping Pokemon Riding info at Slot " + (slot + 1) + " ---")
                    .withStyle(s -> s.withColor(TextColor.fromRgb(0x55FFFF))));
            source.sendFeedback(Component.literal("Species: §f" + pokemon.getSpecies().getName()));
            source.sendFeedback(Component.literal("Species Resource ID: §f" + pokemon.getSpecies().getResourceIdentifier()));
            source.sendFeedback(Component.literal("Form: §f" + pokemon.getForm().getName()));

            LOGGER.info("--- DUMPING RIDING FIELDS FOR POKEMON '{}' at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            final RidingProperties rideProps = pokemon.getRiding();
            ObjectDumper.logObjectFields(LOGGER, rideProps);

            LOGGER.info("--- DUMPING FORM FIELDS FOR POKEMON '{}' at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            final FormData formData = pokemon.getForm();
            ObjectDumper.logObjectFields(LOGGER, formData);

            source.sendFeedback(Component.literal("§eFull object dump sent to console/log."));

            return 1;

        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobble Aid] An unexpected error occurred during dump command!"));
            LOGGER.error("Error executing dump command:", e);
            return 0;
        }
    }

}
