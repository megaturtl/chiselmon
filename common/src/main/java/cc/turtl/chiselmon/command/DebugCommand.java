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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
    public <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal(getName())
                .executes(this::showHelp)
                .then(LiteralArgumentBuilder.<S>literal("test")
                        .executes(this::executeTest))
                .then(LiteralArgumentBuilder.<S>literal("dumpentity")
                        .executes(this::executeDumpEntity));
    }

    private <S> int showHelp(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        String root = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendEmptyLine(player);
        MessageUtils.sendSuccess(player, "Debug Commands");
        MessageUtils.sendPrefixed(player, "  /" + root + " debug test");
        MessageUtils.sendPrefixed(player, "  /" + root + " debug dumpentity");
        return Command.SINGLE_SUCCESS;
    }

    private <S> int executeTest(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        MessageUtils.sendSuccess(player, "Test successful!");
        return Command.SINGLE_SUCCESS;
    }

    private <S> int executeDumpEntity(CommandContext<S> context) {
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
                MessageUtils.sendEmptyLine(player);
                MessageUtils.sendPrefixed(player, PokemonFormats.detailedName(pe.getPokemon(), false));
                MessageUtils.sendLabeled(player, "  NoAI", pe.isNoAi());
                MessageUtils.sendLabeled(player, "  Busy", pe.isBusy());
                MessageUtils.sendLabeled(player, "  Owned", PokemonEntityPredicates.IS_OWNED.test(pe));
                MessageUtils.sendLabeled(player, "  Wild", PokemonEntityPredicates.IS_WILD.test(pe));
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