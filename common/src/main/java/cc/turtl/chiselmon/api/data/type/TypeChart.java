package cc.turtl.chiselmon.api.data.type;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

class TypeChart {

    // Type effectiveness chart: [attacker][defender]
    private static final float[][] CHART = {
            /* Normal    */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5f, 0, 1, 1, 0.5f, 1 },
            /* Fire      */ { 1, 0.5f, 0.5f, 2, 1, 2, 1, 1, 1, 1, 1, 2, 0.5f, 1, 0.5f, 1, 2, 1 },
            /* Water     */ { 1, 2, 0.5f, 0.5f, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5f, 1, 1, 1 },
            /* Grass     */ { 1, 0.5f, 2, 0.5f, 1, 1, 1, 0.5f, 2, 0.5f, 1, 0.5f, 2, 1, 0.5f, 1, 0.5f, 1 },
            /* Electric  */ { 1, 1, 2, 0.5f, 0.5f, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5f, 1, 1, 1 },
            /* Ice       */ { 1, 0.5f, 0.5f, 2, 1, 0.5f, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5f, 1 },
            /* Fighting  */ { 2, 1, 1, 1, 1, 2, 1, 0.5f, 1, 0.5f, 0.5f, 0.5f, 2, 0, 1, 2, 2, 0.5f },
            /* Poison    */ { 1, 1, 1, 2, 1, 1, 1, 0.5f, 0.5f, 1, 1, 1, 0.5f, 0.5f, 1, 1, 0, 2 },
            /* Ground    */ { 1, 2, 1, 0.5f, 2, 1, 1, 2, 1, 0, 1, 0.5f, 2, 1, 1, 1, 2, 1 },
            /* Flying    */ { 1, 1, 1, 2, 0.5f, 1, 2, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 0.5f, 1 },
            /* Psychic   */ { 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5f, 1, 1, 1, 1, 0, 0.5f, 1 },
            /* Bug       */ { 1, 0.5f, 1, 2, 1, 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 0.5f, 1, 2, 0.5f, 0.5f },
            /* Rock      */ { 1, 2, 1, 1, 1, 2, 0.5f, 1, 0.5f, 2, 1, 2, 1, 1, 1, 1, 0.5f, 1 },
            /* Ghost     */ { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 1 },
            /* Dragon    */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5f, 0 },
            /* Dark      */ { 1, 1, 1, 1, 1, 1, 0.5f, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 0.5f },
            /* Steel     */ { 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5f, 2 },
            /* Fairy     */ { 1, 0.5f, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 1, 1, 1, 2, 2, 0.5f, 1 }
    };

    private static final Map<ElementalType, Integer> TYPE_INDICES = new HashMap<>();

    static {
        TYPE_INDICES.put(ElementalTypes.NORMAL, 0);
        TYPE_INDICES.put(ElementalTypes.FIRE, 1);
        TYPE_INDICES.put(ElementalTypes.WATER, 2);
        TYPE_INDICES.put(ElementalTypes.GRASS, 3);
        TYPE_INDICES.put(ElementalTypes.ELECTRIC, 4);
        TYPE_INDICES.put(ElementalTypes.ICE, 5);
        TYPE_INDICES.put(ElementalTypes.FIGHTING, 6);
        TYPE_INDICES.put(ElementalTypes.POISON, 7);
        TYPE_INDICES.put(ElementalTypes.GROUND, 8);
        TYPE_INDICES.put(ElementalTypes.FLYING, 9);
        TYPE_INDICES.put(ElementalTypes.PSYCHIC, 10);
        TYPE_INDICES.put(ElementalTypes.BUG, 11);
        TYPE_INDICES.put(ElementalTypes.ROCK, 12);
        TYPE_INDICES.put(ElementalTypes.GHOST, 13);
        TYPE_INDICES.put(ElementalTypes.DRAGON, 14);
        TYPE_INDICES.put(ElementalTypes.DARK, 15);
        TYPE_INDICES.put(ElementalTypes.STEEL, 16);
        TYPE_INDICES.put(ElementalTypes.FAIRY, 17);
    }

    /**
     * Calculates effectiveness of an attack type against defender type(s).
     * For dual-type defenders, multiplies the effectiveness against each type.
     *
     * @param attacker The attacking type
     * @param defenders The defending type(s) - typically 1 or 2 types
     * @return Effectiveness multiplier (0, 0.25, 0.5, 1, 2, or 4)
     */
    float calculateEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders) {
        Integer attackerIndex = TYPE_INDICES.get(attacker);
        if (attackerIndex == null) {
            return 1.0f;
        }

        float multiplier = 1.0f;
        for (ElementalType defender : defenders) {
            Integer defenderIndex = TYPE_INDICES.get(defender);
            if (defenderIndex != null) {
                multiplier *= CHART[attackerIndex][defenderIndex];
            }
        }

        return multiplier;
    }

    /**
     * Gets the raw chart value for a single type matchup.
     *
     * @param attacker The attacking type
     * @param defender The defending type
     * @return Effectiveness multiplier (0, 0.5, 1, or 2)
     */
    float getChartValue(ElementalType attacker, ElementalType defender) {
        Integer attackerIndex = TYPE_INDICES.get(attacker);
        Integer defenderIndex = TYPE_INDICES.get(defender);

        if (attackerIndex == null || defenderIndex == null) {
            return 1.0f;
        }

        return CHART[attackerIndex][defenderIndex];
    }
}