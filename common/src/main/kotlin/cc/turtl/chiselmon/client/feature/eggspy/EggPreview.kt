package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import cc.turtl.chiselmon.client.config.ChiselmonConfig.general
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon

/** Shared helpers used by render-site mixins to redirect reads on the cached [EggDummy]. */
object EggPreview {

    /**
     * Returns the hatchling preview if [pokemon] is an egg and EggSpy is enabled.
     * Otherwise returns [pokemon] unchanged.
     */
    @JvmStatic
    fun forDisplay(pokemon: Pokemon): Pokemon {
        if (general.modDisabled || !general.eggSpy.enabled) return pokemon
        val duck = pokemon as? DuckPreviewPokemon ?: return pokemon
        return duck.`chiselmon$getPreview`() ?: pokemon
    }

    /**
     * Returns the cached hatchling [RenderablePokemon] if [pokemon] is an egg.
     * Otherwise the pokemon's own renderable.
     */
    @JvmStatic
    fun renderableFor(pokemon: Pokemon): RenderablePokemon {
        val preview = forDisplay(pokemon)
        return if (preview is EggDummy) preview.hatchlingRenderable()
        else pokemon.asRenderablePokemon()
    }

    /**
     * Returns the cached hatchling's hatch ratio for replacement in XP/Health bar, or original if
     * disabled.
     */
    @JvmStatic
    fun eggHatchRatio(original: Float, pokemon: Pokemon): Float {

        if (general.modDisabled || !general.eggSpy.enabled || !general.eggSpy.showHatchOverlay) {
            return original
        }

        val preview = (pokemon as? DuckPreviewPokemon)?.`chiselmon$getPreview`()

        return (preview as? EggDummy)?.let { it.hatchPercentage / 100F } ?: original
    }
}
