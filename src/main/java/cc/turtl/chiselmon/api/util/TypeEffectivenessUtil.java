package cc.turtl.chiselmon.api.util;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;

import java.util.*;

public final class TypeEffectivenessUtil {
    private TypeEffectivenessUtil() {
    }

    private static final float[][] TYPE_CHART = {
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5f, 0, 1, 1, 0.5f, 1 },
            { 1, 0.5f, 0.5f, 1, 2, 2, 1, 1, 1, 1, 1, 2, 0.5f, 1, 0.5f, 1, 2, 1 },
            { 1, 2, 0.5f, 1, 0.5f, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5f, 1, 1, 1 },
            { 1, 1, 2, 0.5f, 0.5f, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5f, 1, 1, 1 },
            { 1, 0.5f, 2, 1, 0.5f, 1, 1, 0.5f, 2, 0.5f, 1, 0.5f, 2, 1, 0.5f, 1, 0.5f, 1 },
            { 1, 0.5f, 0.5f, 1, 2, 0.5f, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5f, 1 },
            { 2, 1, 1, 1, 1, 2, 1, 0.5f, 1, 0.5f, 0.5f, 0.5f, 2, 0, 1, 2, 2, 0.5f },
            { 1, 1, 1, 1, 2, 1, 1, 0.5f, 0.5f, 1, 1, 1, 0.5f, 0.5f, 1, 1, 0, 2 },
            { 1, 2, 1, 2, 0.5f, 1, 1, 2, 1, 0, 1, 0.5f, 2, 1, 1, 1, 2, 1 },
            { 1, 1, 1, 0.5f, 2, 1, 2, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 0.5f, 1 },
            { 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5f, 1, 1, 1, 1, 0, 0.5f, 1 },
            { 1, 0.5f, 1, 1, 2, 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 0.5f, 1, 2, 0.5f, 0.5f },
            { 1, 2, 1, 1, 1, 2, 0.5f, 1, 0.5f, 2, 1, 2, 1, 1, 1, 1, 0.5f, 1 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5f, 0 },
            { 1, 1, 1, 1, 1, 1, 0.5f, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 0.5f },
            { 1, 0.5f, 0.5f, 0.5f, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5f, 2 },
            { 1, 0.5f, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 1, 1, 1, 2, 2, 0.5f, 1 }
    };

    private static final Map<String, Integer> TYPE_INDICES = Map.ofEntries(
            Map.entry("normal", 0), Map.entry("fire", 1), Map.entry("water", 2),
            Map.entry("electric", 3), Map.entry("grass", 4), Map.entry("ice", 5),
            Map.entry("fighting", 6), Map.entry("poison", 7), Map.entry("ground", 8),
            Map.entry("flying", 9), Map.entry("psychic", 10), Map.entry("bug", 11),
            Map.entry("rock", 12), Map.entry("ghost", 13), Map.entry("dragon", 14),
            Map.entry("dark", 15), Map.entry("steel", 16), Map.entry("fairy", 17));

    public static float calculateEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders) {
        if (attacker == null || defenders == null) {
            return 1.0f;
        }

        Integer attackIdx = TYPE_INDICES.get(attacker.getName().toLowerCase());
        if (attackIdx == null) {
            return 1.0f;
        }

        float multiplier = 1.0f;
        for (ElementalType defender : defenders) {
            Integer defIdx = TYPE_INDICES.get(defender.getName().toLowerCase());
            if (defIdx != null) {
                multiplier *= TYPE_CHART[attackIdx][defIdx];
            }
        }
        return multiplier;
    }

    public static boolean isSuperEffective(ElementalType attacker, Iterable<ElementalType> defenders) {
        return calculateEffectiveness(attacker, defenders) > 1.0f;
    }

    public static List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders) {
        if (defenders == null) {
            return List.of();
        }
        return ElementalTypes.all().stream()
                .filter(type -> isSuperEffective(type, defenders))
                .toList();
    }

    public static List<ElementalType> getSuperEffectiveTypes(ElementalType... defenders) {
        return getSuperEffectiveTypes(Arrays.asList(defenders));
    }

    public static Map<ElementalType, Float> getAllEffectiveness(Iterable<ElementalType> defenders) {
        if (defenders == null) {
            return Map.of();
        }
        return ElementalTypes.all().stream()
                .collect(HashMap::new,
                        (map, type) -> map.put(type, calculateEffectiveness(type, defenders)),
                        HashMap::putAll);
    }
}