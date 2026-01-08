package cc.turtl.chiselmon.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.cobblemon.mod.common.api.types.ElementalType;

public class TypeUtil {
    public static Set<ElementalType> iterableToSet(Iterable<ElementalType> iterable) {
        if (iterable instanceof Set)
            return (Set<ElementalType>) iterable;
        if (iterable instanceof Collection)
            return Set.copyOf((Collection<ElementalType>) iterable);

        Set<ElementalType> set = new HashSet<>();
        iterable.forEach(set::add);
        return Collections.unmodifiableSet(set);
    }
}
