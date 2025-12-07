package cc.turtl.cobbleaid.api.capture;

import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import java.util.List;
import java.util.OptionalInt;
import static java.lang.Math.*;

public class CaptureChanceEstimator {

    // Cobblemon config value for max level (approximation for client)
    private static final float MAX_POKEMON_LEVEL = 100.0F;

    // Status Multipliers from the source code
    private static final float STATUS_SLEEP_FROZEN = 2.5F;
    private static final float STATUS_OTHER = 1.5F;
    private static final float STATUS_NONE = 1.0F;

    /**
     * Calculates the estimated probability of a successful capture (P_capture).
     * * @param thrower The player (used for party info).
     * 
     * @param targetEntity The Pokemon Entity being targeted.
     * @param ball         The PokeBall being used.
     * @param playerParty  The list of Pokemon in the player's party.
     * @return A float (0.0F - 1.0F) representing the total capture probability.
     */
    public static float estimateCaptureProbability(
            PokemonEntity targetEntity,
            PokeBall ball) {
        LocalPlayer thrower = Minecraft.getInstance().player;
        Pokemon pokemon = targetEntity.getPokemon();
        List<Pokemon> playerParty = CobblemonClient.INSTANCE.getStorage().getParty().getSlots();

        // --- 1. Master Ball Check ---
        // If the ball bonus is high (Master Ball), return 100% chance.
        if (BallBonusEstimator.calculateBallBonus(ball, targetEntity, thrower, playerParty) >= 999.0F) {
            return 1.0F;
        }

        // --- 2. Input Parameters ---
        // Max HP and Current HP (H, h)
        float H = (float) targetEntity.getMaxHealth();
        float h = (float) targetEntity.getHealth();

        // Base Catch Rate (C)
        float C = (float) pokemon.getForm().getCatchRate();

        // Target Level (L)
        int targetLevel = pokemon.getLevel();

        // Ball Multiplier (B)
        float B = BallBonusEstimator.calculateBallBonus(ball, targetEntity, thrower, playerParty);

        // Status Multiplier (S) !!!! NOT WORKING - need to find where the active in battle status is accessible to the client
        PersistentStatusContainer statusContainer = pokemon.getStatus();

        float S;

        // Check if the status container is null before calling getStatus() on it
        if (statusContainer != null) {
            // If it's not null, safely calculate the bonus using the current status
            S = calculateStatusBonus(statusContainer.getStatus());
        } else {
            // Handle the case where the status container is null (e.g., the Pokémon has no
            // status system)
            // A safe default value is usually 1.0f (no bonus/penalty)
            S = 1.0f;
        }

        // In Battle Modifier (I) - 1.0F in battle, 0.5F otherwise.
        float I = (targetEntity.getBattleId() != null) ? 1.0F : 0.5F;

        // Dark Grass (D) - Hardcoded to 1.0F per source
        float D = 1.0F;

        // Level Bonus (Z) - Gen 9 mechanic for low-level Pokemon
        float Z = calculateLevelBonus(targetLevel);

        // --- 3. Calculate Modified Catch Rate (A) ---

        // The core formula: (3H - 2h) * D * C * I * B / (3H)
        float numerator = (3.0F * H - 2.0F * h) * D * C * I * B;
        float denominator = 3.0F * H;

        // Modified Catch Rate (A) - Pre-multipliers
        float modifiedCatchRate = numerator / denominator;

        // --- 4. Apply Multipliers (S, Z, Difficulty) ---
        modifiedCatchRate *= S * Z;

        // Difficulty Modifier (Level Difference Penalty)
        OptionalInt maxPlayerLevelOpt = playerParty.stream()
                // FIX: Add this line to remove any null Pokemon objects from the stream
                .filter(java.util.Objects::nonNull)
                .mapToInt(Pokemon::getLevel)
                .max();

        if (maxPlayerLevelOpt.isPresent()) {
            int highestLevelThrower = maxPlayerLevelOpt.getAsInt();

            if (highestLevelThrower < targetLevel) {
                float levelDifference = (float) (targetLevel - highestLevelThrower);
                float denominatorForPenalty = MAX_POKEMON_LEVEL / 2.0F;

                float penaltyFactor = 1.0F - (levelDifference / denominatorForPenalty);

                float difficultyModifier = max(0.1F, min(1.0F, penaltyFactor));
                modifiedCatchRate *= difficultyModifier;
            }
        }

        // --- 5. Critical Capture Chance (P_crit) ---
        // Pokedex Progress Multiplier (M_p) - Placeholder for client-side
        float M_p = 1.0F;

        // Crit Chance calculation: min(A * M_p / 12, 255) / 256.0F
        float critChance = min(modifiedCatchRate * M_p / 12.0F, 255.0F) / 256.0F;

        // --- 6. Shake Probability (P_shake) ---
        // P_shake = floor(65536 / (255 / A)^0.1875) / 65536

        // A must be between 1 and 255 for the formula to work correctly.
        float A = min(255.0F, max(1.0F, modifiedCatchRate));

        // Shake probability term
        double exponentTerm = Math.pow(255.0F / A, 0.1875);
        int shakeProbabilityThreshold = (int) floor(65536.0F / exponentTerm);
        float P_shake = (float) shakeProbabilityThreshold / 65536.0F;

        // --- 7. Total Capture Probability (P_capture) ---
        // P_capture = (P_shake^4 * (1 - P_crit)) + (P_crit * P_shake)

        float P_capture_normal = P_shake * P_shake * P_shake * P_shake; // P_shake ^ 4 (4 successful shakes)
        float P_capture_crit = P_shake; // 1 shake needed for critical capture

        // Total Capture Probability
        float P_capture_total = (P_capture_normal * (1.0F - critChance)) + (P_capture_crit * critChance);

        // Ensure the probability is clamped between 0.0 and 1.0
        return min(1.0F, max(0.0F, P_capture_total));
    }

    // ----------------------------------------------------------------------
    // --- Helper Methods ---
    // ----------------------------------------------------------------------

    /**
     * Calculates the status bonus (S).
     */
    private static float calculateStatusBonus(PersistentStatus status) {
        if (status == null)
            return STATUS_NONE;

        if (status instanceof SleepStatus || status instanceof FrozenStatus) {
            return STATUS_SLEEP_FROZEN;
        }
        if (status instanceof ParalysisStatus || status instanceof BurnStatus ||
                status instanceof PoisonStatus || status instanceof PoisonBadlyStatus) {
            return STATUS_OTHER;
        }

        return STATUS_NONE;
    }

    /**
     * Calculates the level bonus (Z) from Gen 9 low-level modifier.
     */
    private static float calculateLevelBonus(int level) {
        if (level < 13) {
            return max((36.0F - (2.0F * level)) / 10.0F, 1.0F);
        }
        return 1.0F;
    }
}