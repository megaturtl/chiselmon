package cc.turtl.chiselmon.core.api.filter

import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.function.Predicate

/**
 * Sealed class hierarchy representing a composable filter condition tree.
 *
 * A filter condition is a tree where:
 * - [And] requires ALL children to match (logical AND)
 * - [Or] requires ANY child to match (logical OR)
 * - [Not] inverts a single child (logical NOT)
 * - [Tag] is a leaf predicate resolved by [FilterTagParser]
 *
 * Conditions are stored as plain strings in [FilterDefinition.conditionString]
 * and parsed at runtime by [FilterConditionParser]. This type is an intermediate
 * representation used only during compilation to a [Predicate].
 */
sealed class FilterCondition {

    /** All children must match. An empty AND is always true. */
    data class And(val children: List<FilterCondition>) : FilterCondition()

    /** At least one child must match. An empty OR is always false. */
    data class Or(val children: List<FilterCondition>) : FilterCondition()

    /** Inverts a single child condition. */
    data class Not(val child: FilterCondition) : FilterCondition()

    /** Leaf node: resolved to a Predicate by [FilterTagParser]. */
    data class Tag(val tag: String) : FilterCondition()

    /**
     * Compiles this condition tree into a [Predicate] for [Pokemon].
     *
     * - AND: all children must pass; empty AND is always true
     * - OR: any child must pass; empty OR is always false
     * - NOT: inverts the child predicate
     * - Tag: delegated to [FilterTagParser]
     */
    fun toPredicate(): Predicate<Pokemon> = when (this) {
        is Tag -> FilterTagParser.parse(tag)
        is Not -> child.toPredicate().negate()
        is And -> children
            .map { it.toPredicate() }
            .reduceOrNull { a, b -> a.and(b) }
            ?: Predicate { true }

        is Or -> children
            .map { it.toPredicate() }
            .reduceOrNull { a, b -> a.or(b) }
            ?: Predicate { false }
    }
}