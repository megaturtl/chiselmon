package cc.turtl.chiselmon.api.calc.type;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;

import java.util.HashMap;
import java.util.Map;

public class TypeCalcs {

    // Type effectiveness chart: [attacker][defender]
    private static final float[][] TYPE_CHART = {
            /* Normal    */ {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5f, 0, 1, 1, 0.5f, 1},
            /* Fire      */ {1, 0.5f, 0.5f, 2, 1, 2, 1, 1, 1, 1, 1, 2, 0.5f, 1, 0.5f, 1, 2, 1},
            /* Water     */ {1, 2, 0.5f, 0.5f, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5f, 1, 1, 1},
            /* Grass     */ {1, 0.5f, 2, 0.5f, 1, 1, 1, 0.5f, 2, 0.5f, 1, 0.5f, 2, 1, 0.5f, 1, 0.5f, 1},
            /* Electric  */ {1, 1, 2, 0.5f, 0.5f, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5f, 1, 1, 1},
            /* Ice       */ {1, 0.5f, 0.5f, 2, 1, 0.5f, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5f, 1},
            /* Fighting  */ {2, 1, 1, 1, 1, 2, 1, 0.5f, 1, 0.5f, 0.5f, 0.5f, 2, 0, 1, 2, 2, 0.5f},
            /* Poison    */ {1, 1, 1, 2, 1, 1, 1, 0.5f, 0.5f, 1, 1, 1, 0.5f, 0.5f, 1, 1, 0, 2},
            /* Ground    */ {1, 2, 1, 0.5f, 2, 1, 1, 2, 1, 0, 1, 0.5f, 2, 1, 1, 1, 2, 1},
            /* Flying    */ {1, 1, 1, 2, 0.5f, 1, 2, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 0.5f, 1},
            /* Psychic   */ {1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5f, 1, 1, 1, 1, 0, 0.5f, 1},
            /* Bug       */ {1, 0.5f, 1, 2, 1, 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 0.5f, 1, 2, 0.5f, 0.5f},
            /* Rock      */ {1, 2, 1, 1, 1, 2, 0.5f, 1, 0.5f, 2, 1, 2, 1, 1, 1, 1, 0.5f, 1},
            /* Ghost     */ {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 1},
            /* Dragon    */ {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5f, 0},
            /* Dark      */ {1, 1, 1, 1, 1, 1, 0.5f, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 0.5f},
            /* Steel     */ {1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5f, 2},
            /* Fairy     */ {1, 0.5f, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 1, 1, 1, 2, 2, 0.5f, 1}
    };

    private static int getTypeIndex(ElementalType type) {
        return type.getTextureXMultiplier();
    }

    /**
     * Computes the effectiveness of all attacking types against the given typing.
     */
    public static TypingMatchups computeMatchups(Iterable<ElementalType> defendingTypes) {
        Map<ElementalType, Float> results = new HashMap<>();

        for (ElementalType attackingType : ElementalTypes.all()) {
            float multiplier = 1.0f;

            for (ElementalType defendingType : defendingTypes) {
                multiplier *= TYPE_CHART[getTypeIndex(attackingType)][getTypeIndex(defendingType)];
            }

            results.put(attackingType, multiplier);
        }

        return new TypingMatchups(results);
    }
}