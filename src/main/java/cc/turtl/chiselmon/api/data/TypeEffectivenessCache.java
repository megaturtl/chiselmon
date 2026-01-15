package cc.turtl.chiselmon.api.data;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cc.turtl.chiselmon.util.TypeUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class TypeEffectivenessCache {
    private TypeEffectivenessCache() {
    }

    private static final float[][] CHART = {
            /* Normal */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5f, 0, 1, 1, 0.5f, 1 },
            /* Fire */ { 1, 0.5f, 0.5f, 2, 1, 2, 1, 1, 1, 1, 1, 2, 0.5f, 1, 0.5f, 1, 2, 1 },
            /* Water */ { 1, 2, 0.5f, 0.5f, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0.5f, 1, 1, 1 },
            /* Grass */ { 1, 0.5f, 2, 0.5f, 1, 1, 1, 0.5f, 2, 0.5f, 1, 0.5f, 2, 1, 0.5f, 1, 0.5f, 1 },
            /* Electric */ { 1, 1, 2, 0.5f, 0.5f, 1, 1, 1, 0, 2, 1, 1, 1, 1, 0.5f, 1, 1, 1 },
            /* Ice */ { 1, 0.5f, 0.5f, 2, 1, 0.5f, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 0.5f, 1 },
            /* Fighting */ { 2, 1, 1, 1, 1, 2, 1, 0.5f, 1, 0.5f, 0.5f, 0.5f, 2, 0, 1, 2, 2, 0.5f },
            /* Poison */ { 1, 1, 1, 2, 1, 1, 1, 0.5f, 0.5f, 1, 1, 1, 0.5f, 0.5f, 1, 1, 0, 2 },
            /* Ground */ { 1, 2, 1, 0.5f, 2, 1, 1, 2, 1, 0, 1, 0.5f, 2, 1, 1, 1, 2, 1 },
            /* Flying */ { 1, 1, 1, 2, 0.5f, 1, 2, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 0.5f, 1 },
            /* Psychic */ { 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0.5f, 1, 1, 1, 1, 0, 0.5f, 1 },
            /* Bug */ { 1, 0.5f, 1, 2, 1, 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 0.5f, 1, 2, 0.5f, 0.5f },
            /* Rock */ { 1, 2, 1, 1, 1, 2, 0.5f, 1, 0.5f, 2, 1, 2, 1, 1, 1, 1, 0.5f, 1 },
            /* Ghost */ { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 1 },
            /* Dragon */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0.5f, 0 },
            /* Dark */ { 1, 1, 1, 1, 1, 1, 0.5f, 1, 1, 1, 2, 1, 1, 2, 1, 0.5f, 1, 0.5f },
            /* Steel */ { 1, 0.5f, 0.5f, 1, 0.5f, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0.5f, 2 },
            /* Fairy */ { 1, 0.5f, 1, 1, 1, 1, 2, 0.5f, 1, 1, 1, 1, 1, 1, 2, 2, 0.5f, 1 }
    };

    private static final Map<ElementalType, Integer> TYPE_TO_INDEX = new IdentityHashMap<>();

    static {
        TYPE_TO_INDEX.put(ElementalTypes.NORMAL, 0);
        TYPE_TO_INDEX.put(ElementalTypes.FIRE, 1);
        TYPE_TO_INDEX.put(ElementalTypes.WATER, 2);
        TYPE_TO_INDEX.put(ElementalTypes.GRASS, 3);
        TYPE_TO_INDEX.put(ElementalTypes.ELECTRIC, 4);
        TYPE_TO_INDEX.put(ElementalTypes.ICE, 5);
        TYPE_TO_INDEX.put(ElementalTypes.FIGHTING, 6);
        TYPE_TO_INDEX.put(ElementalTypes.POISON, 7);
        TYPE_TO_INDEX.put(ElementalTypes.GROUND, 8);
        TYPE_TO_INDEX.put(ElementalTypes.FLYING, 9);
        TYPE_TO_INDEX.put(ElementalTypes.PSYCHIC, 10);
        TYPE_TO_INDEX.put(ElementalTypes.BUG, 11);
        TYPE_TO_INDEX.put(ElementalTypes.ROCK, 12);
        TYPE_TO_INDEX.put(ElementalTypes.GHOST, 13);
        TYPE_TO_INDEX.put(ElementalTypes.DRAGON, 14);
        TYPE_TO_INDEX.put(ElementalTypes.DARK, 15);
        TYPE_TO_INDEX.put(ElementalTypes.STEEL, 16);
        TYPE_TO_INDEX.put(ElementalTypes.FAIRY, 17);
    }

    private static final LoadingCache<Set<ElementalType>, Map<ElementalType, Float>> CACHE = CacheBuilder.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Map<ElementalType, Float> load(Set<ElementalType> defenders) {
                    Map<ElementalType, Float> map = new HashMap<>();
                    for (ElementalType attacker : ElementalTypes.all()) {
                        map.put(attacker, compute(attacker, defenders));
                    }
                    return map;
                }
            });

    private static float compute(ElementalType attacker, Iterable<ElementalType> defenders) {
        Integer atkIdx = TYPE_TO_INDEX.get(attacker);
        if (atkIdx == null)
            return 1.0f;

        float multiplier = 1.0f;
        for (ElementalType defender : defenders) {
            Integer defIdx = TYPE_TO_INDEX.get(defender);
            if (defIdx != null)
                multiplier *= CHART[atkIdx][defIdx];
        }
        return multiplier;
    }

    public static float calculateEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders) {
        if (attacker == null || defenders == null)
            return 1.0f;

        Set<ElementalType> defenderSet = TypeUtil.iterableToSet(defenders);
        if (defenderSet.isEmpty())
            return 1.0f;

        return CACHE.getUnchecked(defenderSet).getOrDefault(attacker, 1.0f);
    }

    public static Map<ElementalType, Float> getAll(Iterable<ElementalType> defenders) {
        if (defenders == null)
            return Map.of();

        Set<ElementalType> defenderSet = TypeUtil.iterableToSet(defenders);
        if (defenderSet.isEmpty())
            return Map.of();

        return CACHE.getUnchecked(defenderSet);
    }

    public static List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders) {
        return getAll(defenders).entrySet().stream()
                .filter(entry -> entry.getValue() > 1.0f)
                // Sort: 4x effectiveness before 2x effectiveness
                .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }
}