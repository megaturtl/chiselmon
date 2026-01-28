package cc.turtl.chiselmon.api.calc.capture;

import static java.lang.Math.*;

/**
 * Calculates capture probability using Gen 9 mechanics.
 * Separated for testability and clarity.
 */
public class CaptureFormulaCalc {

    /**
     * Calculates the total capture probability.
     *
     * @param params The capture parameters
     * @param difficultyModifier Additional difficulty modifier (typically level-based)
     * @return Probability between 0.0 and 1.0
     */
    public float calculate(CaptureParams params, float difficultyModifier) {
        float modifiedCatchRate = calculateModifiedCatchRate(params);
        modifiedCatchRate *= difficultyModifier;

        float critChance = calculateCritChance(modifiedCatchRate, params.pokedexMultiplier());
        float shakeProb = calculateShakeProbability(modifiedCatchRate);

        return calculateTotalProbability(shakeProb, critChance);
    }

    /**
     * Core catch rate formula: (3H - 2h) * D * C * I * B / (3H) * S * Z
     */
    private float calculateModifiedCatchRate(CaptureParams p) {
        float hpFactor = (3.0F * p.maxHp() - 2.0F * p.currentHp()) / (3.0F * p.maxHp());

        return hpFactor
                * p.darkGrassModifier()
                * p.catchRate()
                * p.inBattleModifier()
                * p.ballBonus()
                * p.statusMultiplier()
                * p.levelBonus();
    }

    /**
     * Critical capture chance: min(A * M_p / 12, 255) / 256
     */
    private float calculateCritChance(float modifiedCatchRate, float pokedexMultiplier) {
        return min(modifiedCatchRate * pokedexMultiplier / 12.0F, 255.0F) / 256.0F;
    }

    /**
     * Shake probability: floor(65536 / (255 / A)^0.1875) / 65536
     */
    private float calculateShakeProbability(float modifiedCatchRate) {
        float clampedRate = min(255.0F, max(1.0F, modifiedCatchRate));
        double exponentTerm = pow(255.0F / clampedRate, 0.1875);
        int threshold = (int) floor(65536.0F / exponentTerm);
        return (float) threshold / 65536.0F;
    }

    /**
     * Total probability: (P_shake^4 * (1 - P_crit)) + (P_crit * P_shake)
     */
    private float calculateTotalProbability(float shakeProb, float critChance) {
        float normalCapture = (float) (pow(shakeProb, 4) * (1.0F - critChance));
        float critCapture = shakeProb * critChance;
        float total = normalCapture + critCapture;

        return min(1.0F, max(0.0F, total));
    }
}