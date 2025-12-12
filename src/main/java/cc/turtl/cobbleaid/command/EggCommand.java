package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.format.PokemonFormatUtil;
import cc.turtl.cobbleaid.config.CobbleAidLogger;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;
import cc.turtl.cobbleaid.util.ComponentFormatUtil;
import cc.turtl.cobbleaid.util.StringUtils;
import cc.turtl.cobbleaid.util.ColorUtil;

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

public class EggCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("egg")
                .executes(EggCommand::executeHelp)
                .then(literal("info").then(argument("slot", IntegerArgumentType.integer(1, 6))
                        .executes(EggCommand::executeInfo)));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandFeedbackHelper.sendHeader(source, "Egg Commands");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " egg info <slot>");
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
                CommandFeedbackHelper.sendError(source, "Nothing found at slot " + (slot + 1) + "!");
                return 1;
            }

            Pokemon pokemon = party.get(slot);

            if (!NeoDaycareEgg.isEgg(pokemon)) {
                CommandFeedbackHelper.sendError(source, "Egg not found at slot " + (slot + 1) + "!");
                LOGGER.debug("Egg not found at slot " + (slot + 1) + "!");
                LOGGER.debug("Slot class: '{}'", pokemon.getSpecies().resourceIdentifier.toString());
                return 1;
            }

            LOGGER.debug("Found egg at slot " + (slot + 1) + "!");
            LOGGER.debug("Egg NBT Data: {}", pokemon.getPersistentData());

            NeoDaycareEgg eggData = NeoDaycareEgg.from(pokemon);

            source.sendFeedback(ComponentFormatUtil.colored("--- Egg Info at slot " + (slot + 1) + " ---", ColorUtil.CYAN));
            CommandFeedbackHelper.sendLabeled(source, "Species", eggData.getEgg().getSpecies().getName());
            CommandFeedbackHelper.sendLabeled(source, "Gender", eggData.getEgg().getGender().toString());

            MutableComponent natureName = Component.translatable(eggData.getEgg().getNature().getDisplayName());
            CommandFeedbackHelper.sendLabeled(source, "Nature", natureName);

            MutableComponent abilityName = Component.translatable(eggData.getEgg().getAbility().getDisplayName());
            CommandFeedbackHelper.sendLabeled(source, "Ability", abilityName);

            CommandFeedbackHelper.sendLabeled(source, "Size", String.format("%.2f", eggData.getEgg().getScaleModifier()));
            source.sendFeedback(ComponentFormatUtil.labelledValue("IVs: ", PokemonFormatUtil.hypertrainedIVs(pokemon)));
            CommandFeedbackHelper.sendLabeled(source, "Est. Steps Remaining", String.valueOf(eggData.getStepsRemaining()));
            CommandFeedbackHelper.sendLabeled(source, "Est. Completion", StringUtils.formatPercentage(eggData.getHatchCompletion()));

            return 1;

        } catch (Exception e) {
            CommandFeedbackHelper.sendError(source, "An unexpected error occurred during egg info command!");
            CobbleAid.getLogger().error("Error executing egg info command:", e);
            return 0;
        }
    }
}