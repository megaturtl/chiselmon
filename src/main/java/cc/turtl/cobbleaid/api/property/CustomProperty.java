package cc.turtl.cobbleaid.api.property;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@FunctionalInterface
public interface CustomProperty<T> {
    boolean matches(T item);

    default CustomProperty<T> and(CustomProperty<T> other) {
        return item -> this.matches(item) && other.matches(item);
    }

    default CustomProperty<T> or(CustomProperty<T> other) {
        return item -> this.matches(item) || other.matches(item);
    }

    static <T> CustomProperty<T> not(CustomProperty<T> filter) {
        return item -> !filter.matches(item);
    }
    
    /**
     * Filters a collection to only include items that match this property.
     *
     * @param collection The collection to filter
     * @return A new list containing only items that match this property
     */
    default List<T> filter(Collection<T> collection) {
        return collection.stream()
                .filter(this::matches)
                .collect(Collectors.toList());
    }
}