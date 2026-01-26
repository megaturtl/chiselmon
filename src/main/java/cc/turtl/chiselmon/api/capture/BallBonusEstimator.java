package cc.turtl.chiselmon.api.capture;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.player.client.ClientPokedexManager;
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class BallBonusEstimator {

    // --- Constants ---
    private static final float BASE_BONUS = 1.0f;
    private static final float MASTER_BONUS = 999.0f;
    private static final float NET_BALL_MULTIPLIER = 3.0f;

    // --- Strategy Map keyed by the Cobblemon PokeBall object ---
    private static final Map<PokeBall, Function<CaptureContext, Float>> BALL_STRATEGIES = new HashMap<>();

    static {
        // Standard Balls (Using camelCase getters)
        register(PokeBalls.getPokeBall(), ctx -> BASE_BONUS);
        register(PokeBalls.getGreatBall(), ctx -> 1.5F);
        register(PokeBalls.getUltraBall(), ctx -> 2.0F);
        register(PokeBalls.getSportBall(), ctx -> 1.5F);
        register(PokeBalls.getMasterBall(), ctx -> MASTER_BONUS);

        // Ancient Balls
        register(PokeBalls.getAncientGreatBall(), ctx -> 1.5F);
        register(PokeBalls.getAncientUltraBall(), ctx -> 2.0F);
        register(PokeBalls.getAncientOriginBall(), ctx -> MASTER_BONUS);

        // Balls with No Catch Rate Modifier (Only effects)
        register(PokeBalls.getFriendBall(), ctx -> BASE_BONUS);
        register(PokeBalls.getLuxuryBall(), ctx -> BASE_BONUS);
        register(PokeBalls.getPremierBall(), ctx -> BASE_BONUS);
        register(PokeBalls.getHealBall(), ctx -> BASE_BONUS);
        register(PokeBalls.getCherishBall(), ctx -> BASE_BONUS);

        // Complex Logic Balls (Delegating to methods)
        register(PokeBalls.getParkBall(), BallBonusEstimator::calculateParkBall);
        register(PokeBalls.getSafariBall(), BallBonusEstimator::calculateSafariBall);
        register(PokeBalls.getFastBall(), BallBonusEstimator::calculateFastBall);
        register(PokeBalls.getLevelBall(), BallBonusEstimator::calculateLevelBall);
        register(PokeBalls.getLureBall(), BallBonusEstimator::calculateLureBall);
        register(PokeBalls.getHeavyBall(), BallBonusEstimator::calculateHeavyBall);
        register(PokeBalls.getLoveBall(), BallBonusEstimator::calculateLoveBall);
        register(PokeBalls.getMoonBall(), BallBonusEstimator::calculateMoonBall);
        register(PokeBalls.getNetBall(), BallBonusEstimator::calculateNetBall);
        register(PokeBalls.getDiveBall(), BallBonusEstimator::calculateDiveBall);
        register(PokeBalls.getNestBall(), BallBonusEstimator::calculateNestBall);
        register(PokeBalls.getRepeatBall(), BallBonusEstimator::calculateRepeatBall);
        register(PokeBalls.getTimerBall(), BallBonusEstimator::calculateTimerBall);
        register(PokeBalls.getDuskBall(), BallBonusEstimator::calculateDuskBall);
        register(PokeBalls.getQuickBall(), BallBonusEstimator::calculateQuickBall);
        register(PokeBalls.getDreamBall(), BallBonusEstimator::calculateDreamBall);
        register(PokeBalls.getBeastBall(), BallBonusEstimator::calculateBeastBall);
    }

    private static void register(PokeBall ball, Function<CaptureContext, Float> strategy) {
        BALL_STRATEGIES.put(ball, strategy);
    }

    /**
     * Public entry point for calculating the catch rate bonus using the PokeBall
     * object.
     */
    public static float calculateBallBonus(PokeBall ball, PokemonEntity targetEntity,
            LocalPlayer player, List<ClientBattlePokemon> playerActiveBattlePokemon, PersistentStatus targetStatus) {

        CaptureContext context = new CaptureContext(
                targetEntity,
                targetEntity.getPokemon(),
                playerActiveBattlePokemon,
                targetStatus,
                Minecraft.getInstance().level,
                targetEntity.blockPosition());

        Function<CaptureContext, Float> strategy = BALL_STRATEGIES.getOrDefault(
                ball,
                ctx -> BASE_BONUS);

        return strategy.apply(context);
    }

    // --- Calculation Logic Methods ---

    // Love Ball: Same Species + Opposite Gender (8x), Different Species + Opposite
    // Gender (2.5x)
    private static float calculateLoveBall(CaptureContext ctx) {
        Pokemon target = ctx.pokemon;
        if (target.getGender() == Gender.GENDERLESS)
            return BASE_BONUS;

        boolean foundSameSpeciesOppositeGender = false;
        boolean foundOppositeGender = false;

        for (ClientBattlePokemon p : ctx.playerActiveBattlePokemon) {
            if (p == null || p.getGender() == Gender.GENDERLESS)
                continue;

            if (p.getGender() != target.getGender()) {
                foundOppositeGender = true;
                if (p.getSpecies().equals(target.getSpecies())) {
                    foundSameSpeciesOppositeGender = true;
                    break;
                }
            }
        }

        if (foundSameSpeciesOppositeGender) {
            return 8.0f;
        } else if (foundOppositeGender) {
            return 2.5f;
        }

        return BASE_BONUS;
    }

    // Park Ball: 2.5x in temperate biomes
    private static float calculateParkBall(CaptureContext ctx) {
        var biome = ctx.level.getBiome(ctx.pos);
        return biome.is(CobblemonBiomeTags.IS_TEMPERATE) ? 2.5f : BASE_BONUS;
    }

    // Moon Ball: Time (12k-24k) and Moon Phase
    private static float calculateMoonBall(CaptureContext ctx) {
        long time = ctx.level.getDayTime() % 24000;
        if (time < 12000)
            return BASE_BONUS;

        return switch (ctx.level.getMoonPhase()) {
            case 0 -> 4.0f;
            case 1, 7 -> 2.5f;
            case 2, 6 -> 1.5f;
            default -> BASE_BONUS;
        };
    }

    // Heavy Ball: Weight-based (in Hectograms)
    private static float calculateHeavyBall(CaptureContext ctx) {
        float weight = ctx.pokemon.getForm().getWeight();
        if (weight >= 3000f)
            return 4.0f;
        if (weight >= 2000f)
            return 2.5f;
        if (weight >= 1000f)
            return 1.5f;
        return BASE_BONUS;
    }

    // Dusk Ball: Light Level
    private static float calculateDuskBall(CaptureContext ctx) {
        int brightness = ctx.level.getMaxLocalRawBrightness(ctx.pos);
        if (brightness == 0)
            return 3.5f;
        if (brightness <= 7)
            return 3.0f;
        return BASE_BONUS;
    }

    // Level Ball: Player max level vs. Target level
    private static float calculateLevelBall(CaptureContext ctx) {
        if (ctx.playerActiveBattlePokemon == null || ctx.playerActiveBattlePokemon.isEmpty())
            return BASE_BONUS;

        int maxPlayerLevel = ctx.playerActiveBattlePokemon.stream()
                .mapToInt(ClientBattlePokemon::getLevel)
                .max().orElse(1);

        int targetLevel = ctx.pokemon.getLevel();

        if (maxPlayerLevel > targetLevel * 4)
            return 4.0f;
        if (maxPlayerLevel > targetLevel * 2)
            return 3.0f;
        if (maxPlayerLevel > targetLevel)
            return 2.0f;
        return BASE_BONUS;
    }

    // Lure Ball: 4x if fished
    private static float calculateLureBall(CaptureContext ctx) {
        return ctx.pokemon.getAspects().contains("fished") ? 4.0f : BASE_BONUS;
    }

    // Net Ball: 3x for Bug or Water types
    private static float calculateNetBall(CaptureContext ctx) {
        for (com.cobblemon.mod.common.api.types.ElementalType type : ctx.pokemon.getTypes()) {
            if (type.equals(ElementalTypes.BUG) || type.equals(ElementalTypes.WATER)) {
                return NET_BALL_MULTIPLIER;
            }
        }
        return BASE_BONUS;
    }

    // Dive Ball: 3.5x if submerged in water
    private static float calculateDiveBall(CaptureContext ctx) {
        return ctx.targetEntity.isUnderWater() ? 3.5f : BASE_BONUS;
    }

    // Nest Ball: Level dependent modifier
    private static float calculateNestBall(CaptureContext ctx) {
        int lvl = ctx.pokemon.getLevel();
        if (lvl < 30) {
            return (41.0f - lvl) / 10.0f;
        }
        return BASE_BONUS;
    }

    // Repeat Ball: 3.5x if caught in Pokedex
    private static float calculateRepeatBall(CaptureContext ctx) {
        ClientPokedexManager playerDexData = CobblemonClient.INSTANCE.getClientPokedexData();
        ResourceLocation speciesId = ctx.pokemon.getSpecies().getResourceIdentifier();
        PokedexEntryProgress knowledge = playerDexData.getKnowledgeForSpecies(speciesId);
        if (knowledge == PokedexEntryProgress.CAUGHT) {
            return 3.5f;
        } else
            return 1.0f;
    }

    // Timer Ball: Turn based
    private static float calculateTimerBall(CaptureContext ctx) {
        // this won't be working client side til i figure out how to get the turn.
        UUID battleId = ctx.targetEntity.getBattleId();
        if (battleId == null) {
            return BASE_BONUS;
        }

        PokemonBattle battle = BattleRegistry.getBattle(battleId);
        if (battle == null) {
            return BASE_BONUS;
        }

        int turn = battle.getTurn();

        float baseMultiplier = 1F * turn * (1229F / 4096F);
        return (1F + baseMultiplier) > 4.0F ? 4.0F : (1F + baseMultiplier);
    }

    // Quick Ball: 5x on turn 1
    private static float calculateQuickBall(CaptureContext ctx) {
        // Can't figure out how to get the round client side so just assume it's turn 1 if battling.
        return ctx.targetEntity.isBattling() ? 5.0f : BASE_BONUS;
    }

    // Safari Ball: 1.5x if not battling
    private static float calculateSafariBall(CaptureContext ctx) {
        return !ctx.targetEntity.isBattling() ? 1.5f : BASE_BONUS;
    }

    // Fast Ball: 4x if base speed >= 100
    private static float calculateFastBall(CaptureContext ctx) {
        return ctx.pokemon.getStat(Stats.SPEED) >= 100 ? 4.0f : BASE_BONUS;
    }

    // Dream Ball: 4x if asleep
    private static float calculateDreamBall(CaptureContext ctx) {
        if (ctx.targetStatus instanceof SleepStatus) {
            return 4.0f;
        }
        return BASE_BONUS;
    }

    // Beast Ball: 5x for Ultra Beasts, 0.1x otherwise
    private static float calculateBeastBall(CaptureContext ctx) {
        // 0.1x doesn't seem to be programmed into cobblemon yet, using base bonus for now.
        return PokemonPredicates.IS_ULTRABEAST.test(ctx.pokemon) ? 5.0f : BASE_BONUS;
    }

    // --- Context Object ---
    private static class CaptureContext {
        final PokemonEntity targetEntity;
        final Pokemon pokemon;
        final List<ClientBattlePokemon> playerActiveBattlePokemon;
        final PersistentStatus targetStatus;
        final Level level;
        final BlockPos pos;

        CaptureContext(PokemonEntity t, Pokemon p, List<ClientBattlePokemon> playerActiveBattlePokemon,
                PersistentStatus targetStatus, Level l, BlockPos pos) {
            this.targetEntity = t;
            this.pokemon = p;
            this.playerActiveBattlePokemon = playerActiveBattlePokemon;
            this.targetStatus = targetStatus;
            this.level = l;
            this.pos = pos;
        }
    }
}