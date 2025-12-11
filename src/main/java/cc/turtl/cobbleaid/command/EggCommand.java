package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.formatter.IVsFormatter;
import cc.turtl.cobbleaid.api.util.StringUtil;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class EggCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("egg")
                .executes(EggCommand::executeHelp)
                .then(literal("info").then(argument("slot", IntegerArgumentType.integer(1, 6))
                        .executes(EggCommand::executeInfo)));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§d=== Egg Commands ==="));
        context.getSource()
                .sendFeedback(Component.literal("§7/cobbleaid egg info <slot>"));
        return 1;
    }

    private static int executeInfo(CommandContext<FabricClientCommandSource> context) {
        CobbleAidLogger LOGGER = CobbleAid.getLogger();
        FabricClientCommandSource source = context.getSource();

        try {
            int slot = IntegerArgumentType.getInteger(context, "slot") - 1;

            CobblemonClient client = CobblemonClient.INSTANCE;
            ClientStorageManager storageManager = client.getStorage();
            ClientParty party = storageManager.getParty();

            if (party.get(slot) == null) {
                source.sendFeedback(
                        Component.literal("§c[Cobble Aid] Nothing found at slot " + (slot + 1) + "!"));
                return 1;
            }

            Pokemon pokemon = party.get(slot);

            if (!NeoDaycareEgg.isEgg(pokemon)) {
                source.sendFeedback(
                        Component.literal("§c[Cobble Aid] Egg not found at slot " + (slot + 1) + "!"));
                        LOGGER.debug("Egg not found at slot " + (slot + 1) + "!");
                        LOGGER.debug("Slot class: '{}'", pokemon.getSpecies().resourceIdentifier.toString());
                return 1;
            }

            LOGGER.debug("Found egg at slot " + (slot + 1) + "!");
            LOGGER.debug("Egg NBT Data: {}", pokemon.getPersistentData());

            NeoDaycareEgg eggData = NeoDaycareEgg.from(pokemon);

            source.sendFeedback(Component.literal("--- Egg Info at slot " + (slot + 1) + " ---")
                    .withStyle(s -> s.withColor(TextColor.fromRgb(0x55FFFF))));
            source.sendFeedback(Component.literal("Species: §f" + eggData.getEgg().getSpecies().getName()));
            source.sendFeedback(Component.literal("Gender: §f" + eggData.getEgg().getGender()));

            MutableComponent natureName = Component.translatable(eggData.getEgg().getNature().getDisplayName());
            source.sendFeedback(Component.literal("Nature: §f").append(natureName));

            MutableComponent abilityName = Component.translatable(eggData.getEgg().getAbility().getDisplayName());
            source.sendFeedback(Component.literal("Ability: §f").append(abilityName));

            source.sendFeedback(Component.literal("Size: §f" + String.format("%.2f", eggData.getEgg().getScaleModifier())));
            source.sendFeedback(Component.literal("IVs: " + IVsFormatter.format(eggData.getEgg().getIvs())));
            source.sendFeedback(Component.literal("Est. Steps Remaining: §f" + eggData.getStepsRemaining()));
            source.sendFeedback(Component.literal("Est. Completion: §f" + StringUtil.formatPercentage(eggData.getHatchCompletion())));

            return 1;

        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobble Aid] An unexpected error occurred during egg info command!"));
            LOGGER.error("Error executing egg info command:", e);
            return 0;
        }
    }
}