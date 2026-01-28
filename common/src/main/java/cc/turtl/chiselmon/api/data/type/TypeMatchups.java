package cc.turtl.chiselmon.api.data.type;

import com.cobblemon.mod.common.api.types.ElementalType;

import java.util.*;

/**
 * Immutable result of type effectiveness calculations against a defender.
 * Pre-computes and categorizes all matchups for efficient querying.
 */
class TypeMatchups {

    private final Map<ElementalType, Float> effectivenessMap;
    private final List<ElementalType> superEffective;
    private final List<ElementalType> notVeryEffective;
    private final List<ElementalType> immune;

    TypeMatchups(Map<ElementalType, Float> effectivenessMap) {
        this.effectivenessMap = Map.copyOf(effectivenessMap);

        // Pre-compute categorized lists
        this.superEffective = effectivenessMap.entrySet().stream()
                .filter(e -> e.getValue() > 1.0f)
                .sorted(Comparator.comparing(Map.Entry<ElementalType, Float>::getValue).reversed())
                .map(Map.Entry::getKey)
                .toList();

        this.notVeryEffective = effectivenessMap.entrySet().stream()
                .filter(e -> e.getValue() > 0.0f && e.getValue() < 1.0f)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toList();

        this.immune = effectivenessMap.entrySet().stream()
                .filter(e -> e.getValue() == 0.0f)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Gets the effectiveness multiplier for a specific attacking type.
     */
    float getEffectiveness(ElementalType attacker) {
        return effectivenessMap.getOrDefault(attacker, 1.0f);
    }

    /**
     * Returns the complete effectiveness map.
     */
    Map<ElementalType, Float> asMap() {
        return effectivenessMap;
    }

    /**
     * Gets all super-effective types (>1x), sorted by effectiveness.
     * Order: 4x effectiveness before 2x effectiveness.
     */
    List<ElementalType> getSuperEffective() {
        return superEffective;
    }

    /**
     * Gets all not-very-effective types (<1x but >0x), sorted by effectiveness.
     * Order: 0.25x before 0.5x.
     */
    List<ElementalType> getNotVeryEffective() {
        return notVeryEffective;
    }

    /**
     * Gets all immune types (0x effectiveness).
     */
    List<ElementalType> getImmune() {
        return immune;
    }

    /**
     * Gets all resistances (types that deal ≤1x damage).
     */
    List<ElementalType> getResistances() {
        return effectivenessMap.entrySet().stream()
                .filter(e -> e.getValue() <= 1.0f)
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Gets all weaknesses (types that deal ≥1x damage).
     */
    List<ElementalType> getWeaknesses() {
        return effectivenessMap.entrySet().stream()
                .filter(e -> e.getValue() >= 1.0f)
                .sorted(Comparator.comparing(Map.Entry<ElementalType, Float>::getValue).reversed())
                .map(Map.Entry::getKey)
                .toList();
    }
}