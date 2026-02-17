package cc.turtl.chiselmon.api.calc.capture;

import cc.turtl.chiselmon.api.species.ClientSpecies;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.*;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Estimates the probability of successfully capturing a Pokemon.
 * Uses Gen 9 catch rate mechanics.
 */
public class CaptureChanceEstimator {

    private static final float MAX_POKEMON_LEVEL = 100.0F;

    private final CaptureFormulaCalc formulaCalculator;
    private final BattleContextExtractor battleContextExtractor;

    public CaptureChanceEstimator() {
        this(new CaptureFormulaCalc(), new BattleContextExtractor());
    }

    public CaptureChanceEstimator(CaptureFormulaCalc formulaCalculator,
                                  BattleContextExtractor battleContextExtractor) {
        this.formulaCalculator = formulaCalculator;
        this.battleContextExtractor = battleContextExtractor;
    }

    private static float getCatchRate(Pokemon pokemon) {
        ClientSpecies species = ClientSpeciesRegistry.get(pokemon.getSpecies().getName());
        return species != null ? species.catchRate() : 0f;
    }

    private static float calculateStatusMultiplier(PersistentStatus status) {
        final float SLEEP_FROZEN = 2.5F;
        final float OTHER_STATUS = 1.5F;
        final float NO_STATUS = 1.0F;

        if (status == null) {
            return NO_STATUS;
        }

        if (status instanceof SleepStatus || status instanceof FrozenStatus) {
            return SLEEP_FROZEN;
        }

        if (status instanceof ParalysisStatus || status instanceof BurnStatus ||
                status instanceof PoisonStatus || status instanceof PoisonBadlyStatus) {
            return OTHER_STATUS;
        }

        return NO_STATUS;
    }

    /**
     * Calculates the estimated probability of a successful capture.
     *
     * @param targetEntity The Pokemon Entity being targeted
     * @param ball         The PokeBall being used
     * @return A float (0.0F - 1.0F) representing the capture probability
     */
    public float estimateCaptureProbability(PokemonEntity targetEntity, PokeBall ball) {
        var player = Minecraft.getInstance().player;
        var pokemon = targetEntity.getPokemon();
        var playerParty = CobblemonClient.INSTANCE.getStorage().getParty().getSlots();

        // Extract battle context
        var battleContext = battleContextExtractor.extract(player, targetEntity);

        // Calculate ball bonus
        float ballBonus = BallBonusCalc.calculateBallBonus(
                ball, targetEntity, player,
                battleContext.playerActivePokemon(),
                battleContext.targetStatus()
        );

        // Master Ball or equivalent
        if (ballBonus >= 999.0F) {
            return 1.0F;
        }

        // Build capture parameters
        var params = CaptureParams.builder()
                .maxHp(targetEntity.getMaxHealth())
                .currentHp(targetEntity.getHealth())
                .catchRate(getCatchRate(pokemon))
                .targetLevel(pokemon.getLevel())
                .statusMultiplier(calculateStatusMultiplier(battleContext.targetStatus()))
                .inBattleModifier(targetEntity.getBattleId() != null ? 1.0F : 0.5F)
                .levelBonus(calculateLevelBonus(pokemon.getLevel()))
                .ballBonus(ballBonus)
                .build();

        // Apply difficulty modifier based on party level
        float difficultyModifier = calculateDifficultyModifier(playerParty, pokemon.getLevel());

        return formulaCalculator.calculate(params, difficultyModifier);
    }

    private float calculateLevelBonus(int level) {
        final int BONUS_LEVEL_THRESHOLD = 13;

        if (level < BONUS_LEVEL_THRESHOLD) {
            return Math.max((36.0F - (2.0F * level)) / 10.0F, 1.0F);
        }
        return 1.0F;
    }

    private float calculateDifficultyModifier(List<Pokemon> playerParty, int targetLevel) {
        return playerParty.stream()
                .filter(java.util.Objects::nonNull)
                .mapToInt(Pokemon::getLevel)
                .max()
                .stream()
                .mapToObj(maxLevel -> computePenalty(maxLevel, targetLevel))
                .findFirst()
                .orElse(1.0F);
    }

    private float computePenalty(int maxPlayerLevel, int targetLevel) {
        if (maxPlayerLevel >= targetLevel) {
            return 1.0F;
        }

        float levelDifference = targetLevel - maxPlayerLevel;
        float penaltyFactor = 1.0F - (levelDifference / (MAX_POKEMON_LEVEL / 2.0F));
        return Math.max(0.1F, Math.min(1.0F, penaltyFactor));
    }
}