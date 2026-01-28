package cc.turtl.chiselmon.api.data.type;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Efficient type effectiveness calculator with caching.
 * Thread-safe and optimized for repeated queries against the same defender type combinations.
 */
public final class TypeEffectivenessCache {

    private TypeEffectivenessCache() {}

    // Singleton instance for global access
    private static final TypeEffectivenessCache INSTANCE = new TypeEffectivenessCache();

    public static TypeEffectivenessCache getInstance() {
        return INSTANCE;
    }

    private final TypeChart chart = new TypeChart();

    private final LoadingCache<DefenderTypes, TypeMatchups> cache = CacheBuilder.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public TypeMatchups load(DefenderTypes defenders) {
                    return computeAllMatchups(defenders);
                }
            });

    /**
     * Calculates type effectiveness for a single attacker against defender(s).
     *
     * @param attacker The attacking type
     * @param defenders The defending type(s) - can be 1 or 2 types
     * @return Effectiveness multiplier (0, 0.25, 0.5, 1, 2, or 4)
     */
    public float calculateEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders) {
        if (attacker == null || defenders == null) {
            return 1.0f;
        }

        DefenderTypes defenderTypes = DefenderTypes.from(defenders);
        if (defenderTypes.isEmpty()) {
            return 1.0f;
        }

        return cache.getUnchecked(defenderTypes).getEffectiveness(attacker);
    }

    /**
     * Gets all type matchups for the given defender type(s).
     *
     * @param defenders The defending type(s)
     * @return Map of attacker type -> effectiveness multiplier
     */
    public Map<ElementalType, Float> getAllMatchups(Iterable<ElementalType> defenders) {
        if (defenders == null) {
            return Map.of();
        }

        DefenderTypes defenderTypes = DefenderTypes.from(defenders);
        if (defenderTypes.isEmpty()) {
            return Map.of();
        }

        return cache.getUnchecked(defenderTypes).asMap();
    }

    /**
     * Gets all super-effective attacking types against the defender(s).
     * Results are sorted by effectiveness (4x before 2x).
     *
     * @param defenders The defending type(s)
     * @return List of super-effective types, sorted by effectiveness
     */
    public List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders) {
        if (defenders == null) {
            return List.of();
        }

        DefenderTypes defenderTypes = DefenderTypes.from(defenders);
        if (defenderTypes.isEmpty()) {
            return List.of();
        }

        return cache.getUnchecked(defenderTypes).getSuperEffective();
    }

    /**
     * Gets all not-very-effective attacking types against the defender(s).
     *
     * @param defenders The defending type(s)
     * @return List of not-very-effective types
     */
    public List<ElementalType> getNotVeryEffectiveTypes(Iterable<ElementalType> defenders) {
        DefenderTypes defenderTypes = DefenderTypes.from(defenders);
        if (defenderTypes.isEmpty()) {
            return List.of();
        }

        return cache.getUnchecked(defenderTypes).getNotVeryEffective();
    }

    /**
     * Gets all immune (0x effectiveness) attacking types against the defender(s).
     *
     * @param defenders The defending type(s)
     * @return List of immune types
     */
    public List<ElementalType> getImmuneTypes(Iterable<ElementalType> defenders) {
        DefenderTypes defenderTypes = DefenderTypes.from(defenders);
        if (defenderTypes.isEmpty()) {
            return List.of();
        }

        return cache.getUnchecked(defenderTypes).getImmune();
    }

    private TypeMatchups computeAllMatchups(DefenderTypes defenders) {
        Map<ElementalType, Float> matchups = new HashMap<>();

        for (ElementalType attacker : ElementalTypes.all()) {
            float effectiveness = chart.calculateEffectiveness(attacker, defenders.types());
            matchups.put(attacker, effectiveness);
        }

        return new TypeMatchups(matchups);
    }

    // ========== Static Convenience Methods ==========

    public static float getEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders) {
        return INSTANCE.calculateEffectiveness(attacker, defenders);
    }

    public static Map<ElementalType, Float> getAll(Iterable<ElementalType> defenders) {
        return INSTANCE.getAllMatchups(defenders);
    }

    public static List<ElementalType> getSuperEffective(Iterable<ElementalType> defenders) {
        return INSTANCE.getSuperEffectiveTypes(defenders);
    }

    public static List<ElementalType> getNotVeryEffective(Iterable<ElementalType> defenders) {
        return INSTANCE.getNotVeryEffectiveTypes(defenders);
    }

    public static List<ElementalType> getImmune(Iterable<ElementalType> defenders) {
        return INSTANCE.getImmuneTypes(defenders);
    }
}