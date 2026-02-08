package cc.turtl.chiselmon.api.calc.type;

import com.cobblemon.mod.common.api.types.ElementalType;

import java.util.*;
import java.util.stream.Stream;

/**
 * Maps every attacking type to its damage multiplier against a typing.
 */
public record TypingMatchups(Map<ElementalType, Float> multipliers) {

    public TypingMatchups {
        multipliers = Collections.unmodifiableMap(multipliers);
    }

    public List<ElementalType> getSuperWeak() {
        return filterByMultiplier(4.0f);
    }

    public List<ElementalType> getWeak() {
        return filterByMultiplier(2.0f);
    }

    public List<ElementalType> getAllWeak() {
        return Stream.of(getSuperWeak(), getWeak())
                .flatMap(List::stream)
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

    public List<ElementalType> getAllResist() {
        return Stream.of(getResistant(), getSuperResistant(), getImmune())
                .flatMap(List::stream)
                .toList();
    }

    private List<ElementalType> filterByMultiplier(float target) {
        return multipliers.entrySet().stream()
                .filter(entry -> entry.getValue() == target)
                .map(Map.Entry::getKey)
                .toList();
    }
}