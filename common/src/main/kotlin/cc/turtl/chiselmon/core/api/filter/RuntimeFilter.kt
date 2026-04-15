package cc.turtl.chiselmon.core.api.filter

import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.turtlshell.api.core.Priority
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component
import java.util.function.Predicate

/**
 * Represents a compiled, named filter ready to match against pokemon.
 *
 * @param id        Unique identifier (e.g. "legendaries", "custom_tiny_shinies")
 * @param name      Pretty display name string
 * @param rgb       RGB color used for text and glow effects
 * @param priority  Alert priority for this filter
 * @param condition Predicate that determines if a Pokemon passes this filter
 */
data class RuntimeFilter(
    @JvmField val id: String,
    @JvmField val name: String,
    @JvmField val rgb: Int,
    @JvmField val priority: Priority,
    @JvmField val condition: Predicate<Pokemon>
) {
    fun matches(pokemon: Pokemon): Boolean = condition.test(pokemon)

    fun displayName(): Component = createComponent(name, rgb, false)
}