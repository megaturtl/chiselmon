package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.CobbleAidLogger;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import cc.turtl.cobbleaid.util.ObjectDumper;

public class DebugCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("debug")
                .executes(DebugCommand::executeHelp)
                .then(literal("dump")
                        .then(argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes(DebugCommand::executeDump))
                .then(literal("look").executes(DebugCommand::executeLookDump)));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§d=== Debug Commands ==="));
        context.getSource()
                .sendFeedback(Component.literal("§7/cobbleaid debug dump <slot> §f- Dumps info about provided slot."));
        context.getSource()
                .sendFeedback(Component.literal("§7/cobbleaid debug dump look §f- Dumps info about targeted pokemon entity."));
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
            source.sendFeedback(
                    Component.literal("Species Resource ID: §f" + pokemon.getSpecies().getResourceIdentifier()));
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

    private static int executeLookDump(CommandContext<FabricClientCommandSource> context) {
        CobbleAidLogger LOGGER = CobbleAid.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            source.sendFeedback(Component.literal("--- Dumping Targeted Entity ---")
                    .withStyle(s -> s.withColor(TextColor.fromRgb(0x55FFFF))));
            Minecraft minecraftClient = Minecraft.getInstance();
            Entity lookingAtEntity = minecraftClient.crosshairPickEntity;

            if (lookingAtEntity == null) {
                source.sendError(Component.literal("§c[Cobble Aid] Not looking at a pokemon!"));
                return 0;
            }

            if (lookingAtEntity instanceof PokemonEntity pokemonEntity) {
                Pokemon pokemon = pokemonEntity.getPokemon();

                source.sendFeedback(Component.literal("--- Dumping Targeted POKEMON Object ---")
                        .withStyle(s -> s.withColor(TextColor.fromRgb(0x55FF55))));
                ObjectDumper.logObjectFields(LOGGER, pokemon);
                source.sendFeedback(Component.literal("§eFull object dump sent to console/log."));
                return 1;

            } else {
                source.sendError(Component.literal("§c[Cobble Aid] Targeted entity is a "
                        + lookingAtEntity.getClass().getSimpleName() + ", not a Pokémon."));
                return 0;
            }

        } catch (Exception e) {
            source.sendError(
                    Component.literal("§c[Cobble Aid] An unexpected error occurred during look dump command!"));
            LOGGER.error("Error executing dump look command:", e);
            return 0;
        }
    }
}
