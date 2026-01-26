package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.data.SimpleSpecies;
import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.CommandUtils;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.ObjectDumper;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.Logger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
        CommandUtils.sendUsageWithDescription(source, "/" + ChiselmonConstants.MODID + " debug dump <slot>",
                "Dumps info about provided slot.");
        CommandUtils.sendUsageWithDescription(source, "/" + ChiselmonConstants.MODID + " debug dump look",
                "Dumps info about targeted pokemon entity.");
        return 1;
    }

    private static int executeTest(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(ComponentUtil.colored("Testing random things", ColorUtil.AQUA));
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
                    ComponentUtil.colored("--- Dumping Pokemon at Slot " + (slot + 1) + " ---", ColorUtil.AQUA));
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
            Minecraft minecraftClient = Minecraft.getInstance();
            Entity lookingAtEntity = minecraftClient.crosshairPickEntity;

            if (lookingAtEntity == null) {
                CommandUtils.sendError(source, "Not looking at a pokemon!");
                return 0;
            }

            if (lookingAtEntity instanceof PokemonEntity pokemonEntity) {

                source.sendFeedback(
                        ComponentUtil.colored("--- Dumping Targeted PokemonEntity Object ---", ColorUtil.GREEN));
                CommandUtils.sendLabeled(source, "isNoAi", pokemonEntity.isNoAi());
                CommandUtils.sendLabeled(source, "isAttackable", pokemonEntity.isAttackable());
                CommandUtils.sendLabeled(source, "isBusy", pokemonEntity.isBusy());
                CommandUtils.sendLabeled(source, "isEffectiveAi", pokemonEntity.isEffectiveAi());
                CommandUtils.sendLabeled(source, "isInvulnerable", pokemonEntity.isInvulnerable());
                CommandUtils.sendLabeled(source, "isPushable", pokemonEntity.isPushable());
                CommandUtils.sendLabeled(source, "isUncatchable", pokemonEntity.isUncatchable());
                CommandUtils.sendLabeled(source, "scoreboardName", pokemonEntity.getScoreboardName());
                CommandUtils.sendLabeled(source, "hasImpulse", pokemonEntity.hasImpulse);
                CommandUtils.sendLabeled(source, "horizontalCollision", pokemonEntity.horizontalCollision);
                CommandUtils.sendLabeled(source, "moveDist", pokemonEntity.moveDist);
                CommandUtils.sendLabeled(source, "noCulling", pokemonEntity.noCulling);
                CommandUtils.sendLabeled(source, "walkDist", pokemonEntity.walkDist);
                CommandUtils.sendLabeled(source, "tickCount", pokemonEntity.tickCount);
                CommandUtils.sendLabeled(source, "timeOffs", pokemonEntity.timeOffs);
                CommandUtils.sendLabeled(source, "getX", pokemonEntity.getX());
                CommandUtils.sendLabeled(source, "getY", pokemonEntity.getY());
                CommandUtils.sendLabeled(source, "getZ", pokemonEntity.getZ());
                CommandUtils.sendLabeled(source, "getXRot", pokemonEntity.getXRot());
                CommandUtils.sendLabeled(source, "attackable", pokemonEntity.attackable());
                CommandUtils.sendLabeled(source, "canWalk", pokemonEntity.canWalk());
                CommandUtils.sendLabeled(source, "age", pokemonEntity.getAge());
                CommandUtils.sendLabeled(source, "aspects", pokemonEntity.getAspects());
                CommandUtils.sendLabeled(source, "displayName", pokemonEntity.getDisplayName());
                CommandUtils.sendLabeled(source, "tickSpawned", pokemonEntity.getTickSpawned());
                CommandUtils.sendLabeled(source, "ticksLived", pokemonEntity.getTicksLived());
                CommandUtils.sendLabeled(source, "titledName", pokemonEntity.getTitledName());
                CommandUtils.sendLabeled(source, "scoreboardName", pokemonEntity.getScoreboardName());

                source.sendFeedback(
                        ComponentUtil.colored("- Chiselmon Predicates -", ColorUtil.GREEN));
                CommandUtils.sendLabeled(source, "IS_OWNED", PokemonEntityPredicates.IS_OWNED.test(pokemonEntity));
                CommandUtils.sendLabeled(source, "IS_BOSS", PokemonEntityPredicates.IS_BOSS.test(pokemonEntity));
                CommandUtils.sendLabeled(source, "IS_WILD", PokemonEntityPredicates.IS_WILD.test(pokemonEntity));
                LOGGER.info("Logging object details for targeted Pokemon...");
                ObjectDumper.logObjectFields(LOGGER, pokemonEntity);
                CommandUtils.sendWarning(source, "Full object dump sent to console/log.");
                return 1;

            } else {
                CommandUtils.sendWarning(source, "Targeted entity is a "
                        + lookingAtEntity.getClass().getSimpleName() + ", not a Pokemon.");

                LOGGER.info("Logging object details for targeted entity...");
                ObjectDumper.logObjectFields(LOGGER, lookingAtEntity);
                CommandUtils.sendWarning(source, "Full object dump sent to console/log.");
                return 1;
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
                    ComponentUtil.colored("--- Dumping Species " + species.name + " ---", ColorUtil.AQUA));
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