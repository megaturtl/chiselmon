package cc.turtl.chiselmon.api.filter;

import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.List;
import java.util.function.Predicate;

/**
 * Sealed tree node hierarchy representing a composable filter condition.
 *
 * <p>A filter condition is a tree where:
 * <ul>
 *   <li>{@link And} - ALL children must match (logical AND)</li>
 *   <li>{@link Or}  - ANY child must match (logical OR)</li>
 *   <li>{@link Not} - inverts a single child (logical NOT)</li>
 *   <li>{@link Tag} - a leaf predicate resolved by {@link FilterTagParser}</li>
 * </ul>
 *
 * <p>Conditions are stored as plain strings in {@link FilterDefinition#conditionString}
 * and parsed at runtime by {@link FilterConditionParser}. This type is an intermediate
 * representation used only during compilation to a {@link Predicate}.
 */
public sealed interface FilterCondition permits
        FilterCondition.And,
        FilterCondition.Or,
        FilterCondition.Not,
        FilterCondition.Tag {

    /** All children must match. An empty AND is always true. */
    record And(List<FilterCondition> children) implements FilterCondition {
        public And {
            // Canonical constructor: enforce immutability and null-safety
            children = children != null ? List.copyOf(children) : List.of();
        }
    }

    /** At least one child must match. An empty OR is always false. */
    record Or(List<FilterCondition> children) implements FilterCondition {
        public Or {
            children = children != null ? List.copyOf(children) : List.of();
        }
    }

    /** Inverts a single child condition. */
    record Not(FilterCondition child) implements FilterCondition {}

    /** Leaf node: resolved to a Predicate by {@link FilterTagParser}. */
    record Tag(String tag) implements FilterCondition {
        public Tag {
            tag = tag != null ? tag : "";
        }
    }
    /**
     * Compiles this condition tree into a {@link Predicate}{@code <Pokemon>}.
     *
     * <ul>
     *   <li>AND -  all children must pass; empty AND is always true</li>
     *   <li>OR  - any child must pass; empty OR is always false</li>
     *   <li>NOT - inverts the child predicate</li>
     *   <li>Tag - delegated to {@link FilterTagParser}</li>
     * </ul>
     */
    default Predicate<Pokemon> toPredicate() {
        return switch (this) {
            case Tag tag   -> FilterTagParser.parse(tag.tag());
            case Not not   -> not.child().toPredicate().negate();
            case And and   -> and.children().stream()
                    .map(FilterCondition::toPredicate)
                    .reduce(Predicate::and)
                    .orElse(p -> true);
            case Or or     -> or.children().stream()
                    .map(FilterCondition::toPredicate)
                    .reduce(Predicate::or)
                    .orElse(p -> false);
        };
    }
}