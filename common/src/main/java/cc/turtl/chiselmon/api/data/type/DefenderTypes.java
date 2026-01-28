package cc.turtl.chiselmon.api.data.type;

import com.cobblemon.mod.common.api.types.ElementalType;

import java.util.*;

/**
 * Immutable set of defender types, used as cache key.
 * Normalized to ensure consistent hashing for dual-type Pokemon.
 */
record DefenderTypes(Set<ElementalType> types) {

    // Compact constructor for validation
    DefenderTypes {
        if (types.size() > 2) {
            throw new IllegalArgumentException("Pokemon can have at most 2 types, got: " + types.size());
        }
        // Make defensive copy and ensure immutability
        types = types.isEmpty() ? Set.of() : Set.copyOf(types);
    }

    /**
     * Creates a DefenderTypes from an iterable, filtering nulls and duplicates.
     */
    static DefenderTypes from(Iterable<ElementalType> types) {
        Set<ElementalType> typeSet = new HashSet<>();
        for (ElementalType type : types) {
            if (type != null) {
                typeSet.add(type);
            }
        }
        return new DefenderTypes(typeSet);
    }

    boolean isEmpty() {
        return types.isEmpty();
    }

    int size() {
        return types.size();
    }

    /**
     * Check if this represents a dual-type Pokemon.
     */
    boolean isDualType() {
        return types.size() == 2;
    }
}