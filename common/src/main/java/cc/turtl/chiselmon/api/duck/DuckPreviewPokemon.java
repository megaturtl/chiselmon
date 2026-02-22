package cc.turtl.chiselmon.api.duck;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;

/**
 * Adds methods to a Pokemon to get more detailed data
 */
public interface DuckPreviewPokemon {

    boolean chiselmon$isEgg();

    /**
     * Returns the dummy if this is an egg, otherwise returns 'this'.
     */
    Pokemon chiselmon$getPreview();

    /**
     * Bypasses preview/dummy logic to get the original renderable pokemon.
     */
    RenderablePokemon chiselmon$getRawRenderablePokemon();
}