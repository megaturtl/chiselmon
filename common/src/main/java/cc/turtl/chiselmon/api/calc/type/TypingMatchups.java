package cc.turtl.chiselmon.api.calc.type;

import com.cobblemon.mod.common.api.types.ElementalType;

import java.util.*;

/**
 * Maps every attacking type to its damage multiplier against a typing.
 */
public record TypingMatchups(Map<ElementalType, Float> multiplierMap) {

    public TypingMatchups {
        multiplierMap = Map.copyOf(multiplierMap);
    }

    public List<ElementalType> getSuperWeak() {
        return filterByMultiplier(4.0f);
    }

    public List<ElementalType> getWeak() {
        return filterByMultiplier(2.0f);
    }

    /**
     * Gets all types that deal > 1.0x damage, sorted (4x then 2x).
     */
    public List<ElementalType> getAllWeak() {
        return multiplierMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1.0f)
                .sorted(Map.Entry.<ElementalType, Float>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<ElementalType> getResistant() {
        return filterByMultiplier(0.5f);
    }

    public List<ElementalType> getSuperResistant() {
        return filterByMultiplier(0.25f);
    }

    public List<ElementalType> getImmune() {
        return filterByMultiplier(0.0f);
    }

    /**
     * Gets all types that deal < 1.0x damage, sorted (0x, then 0.25x, then 0.5x).
     */
    public List<ElementalType> getAllResist() {
        return multiplierMap.entrySet().stream()
                .filter(entry -> entry.getValue() < 1.0f)
                // Sort ascending (0.0, 0.25, 0.5)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<ElementalType> filterByMultiplier(float target) {
        return multiplierMap.entrySet().stream()
                .filter(entry -> entry.getValue() == target)
                .map(Map.Entry::getKey)
                .toList();
    }
}