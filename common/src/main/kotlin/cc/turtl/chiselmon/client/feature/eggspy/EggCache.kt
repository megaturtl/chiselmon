package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.common.cache.CacheBuilder
import java.util.UUID
import java.util.concurrent.TimeUnit

object EggCache {
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build<UUID, EggDummy>()

    /**
     * Returns the original pokemon, or its egg preview if available.
     * Live progress fields are refreshed from the given pokemon on every call,
     * so the dummy stays accurate without the whole object needing to be invalidated.
     */
    @JvmStatic
    fun getPreview(pokemon: Pokemon): Pokemon {
        if (pokemon is EggDummy) return pokemon
        if (!(pokemon as DuckPreviewPokemon).`chiselmon$isEgg`()) return pokemon

        val uuid = pokemon.uuid
        val dummy = cache.getIfPresent(uuid)
            ?: EggDummy.from(pokemon)?.also { cache.put(uuid, it) }
            ?: return pokemon

        dummy.totalSteps = pokemon.persistentData.getInt("TotalSteps")
        dummy.hatchPercentage = pokemon.getFeature<IntSpeciesFeature>(EggDummy.HATCH_PERCENTAGE_FEATURE)?.value ?: 0

        return dummy
    }

    @JvmStatic
    fun invalidate(uuid: UUID) = cache.invalidate(uuid)
}