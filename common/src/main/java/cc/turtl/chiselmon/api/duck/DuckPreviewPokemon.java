package cc.turtl.chiselmon.api.duck;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import org.spongepowered.asm.mixin.Unique;

/** Adds methods to a Pokemon to get more detailed data
 */
public interface DuckPreviewPokemon {

    boolean chiselmon$isEgg();

    /** Returns the dummy if this is an egg, otherwise returns 'this'.
     */
    Pokemon chiselmon$getPreview();
}