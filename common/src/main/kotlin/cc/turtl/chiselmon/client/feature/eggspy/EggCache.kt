package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.common.cache.CacheBuilder
import java.util.*
import java.util.concurrent.TimeUnit

object EggCache {
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build<UUID, EggDummy>()

    /**
     * Returns the original pokemon, or its egg preview if available.
     */
    @JvmStatic
    fun getPreview(pokemon: Pokemon): Pokemon {
        if (pokemon is EggDummy) return pokemon
        if (!(pokemon as DuckPreviewPokemon).`chiselmon$isEgg`()) return pokemon

        val uuid = pokemon.uuid
        cache.getIfPresent(uuid)?.let { return it }

        return EggDummy.from(pokemon)
            ?.also { cache.put(uuid, it) }
            ?: pokemon
    }

    @JvmStatic
    fun invalidate(uuid: UUID) = cache.invalidate(uuid)
}