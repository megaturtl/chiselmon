package cc.turtl.chiselmon.client.api.duck

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon

/**
 * Adds methods to a Pokemon to get more detailed data
 */
interface DuckPreviewPokemon {
    fun `chiselmon$isEgg`(): Boolean

    /**
     * Returns the dummy if this is an egg, otherwise returns 'this'.
     */
    fun `chiselmon$getPreview`(): Pokemon?

    /**
     * Bypasses preview/dummy logic to get the original renderable pokemon.
     */
    fun `chiselmon$getRawRenderablePokemon`(): RenderablePokemon?
}