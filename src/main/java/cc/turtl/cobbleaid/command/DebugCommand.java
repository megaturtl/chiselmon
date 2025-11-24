package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import java.lang.reflect.Field;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.CobbleAidLogger;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class DebugCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("debug")
                .executes(DebugCommand::executeHelp)
                .then(literal("dump")
                    .then(argument("slot", IntegerArgumentType.integer(1, 6))
                        .executes(DebugCommand::executeDump)));
    }
    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§6§l=== Debug Commands ==="));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid debug dump <slot> §f- Dumps info about current slot."));
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
                source.sendFeedback(Component.literal("§c[CobbleAid] Party slot " + (slot + 1) + " is empty."));
                return 1;
            }

            // --- CHAT FEEDBACK (Visible to user) ---
            source.sendFeedback(Component.literal("--- Dumping Pokemon at Slot " + (slot + 1) + " ---").withStyle(s -> s.withColor(TextColor.fromRgb(0x55FFFF))));
            source.sendFeedback(Component.literal("Species: §f" + pokemon.getSpecies().getName()));
            source.sendFeedback(Component.literal("Class: §f" + pokemon.getClass().getName()));
            source.sendFeedback(Component.literal("Level: §f" + pokemon.getLevel()));
            
            // --- CONSOLE/LOG DUMP (Detailed internal data) ---
            LOGGER.info("--- DUMPING FIELDS FOR POKEMON {} at slot {} ---", pokemon.getSpecies().getName(), slot + 1);
            
            // Loop through all declared fields in the Pokemon object and its superclasses
            Class<?> currentClass = pokemon.getClass();
            
            // Traverse the class hierarchy up to, but not including, java.lang.Object
            while (currentClass != null && currentClass != Object.class) {
                LOGGER.info("Fields in {}:", currentClass.getName());
                
                for (Field field : currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(pokemon);
                        // Print the field name and its value to the console/log
                        LOGGER.info("  {}: {}", field.getName(), value);
                    } catch (IllegalAccessException e) {
                        LOGGER.error("  Error accessing field {}: {}", field.getName(), e.getMessage());
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            source.sendFeedback(Component.literal("§eFull object dump sent to console/log."));

            return 1;

        } catch (Exception e) {
            source.sendError(Component.literal("§c[CobbleAid] An unexpected error occurred during dump: " + e.getMessage()));
            LOGGER.error("Error executing /ca debug dump command:", e);
            return 0;
        }
    }
    
}
