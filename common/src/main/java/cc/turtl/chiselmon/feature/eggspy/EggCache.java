package cc.turtl.chiselmon.feature.eggspy;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EggCache {
    private static final Cache<UUID, EggDummy> EGGS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    /**
     * Returns back the original pokemon, or its egg preview if possible.
     * This function assumes the mod and eggspy feature is enabled, check this before calling!
     */
    public static Pokemon getPreview(Pokemon pokemon) {
        if (pokemon instanceof EggDummy) return pokemon;
        if (!((DuckPreviewPokemon) pokemon).chiselmon$isEgg()) return pokemon;

        UUID uuid = pokemon.getUuid();
        EggDummy cached = EGGS_CACHE.getIfPresent(uuid);

        if (cached != null) {
            return cached;
        }

        // Try to generate, return original if parsing fails
        return EggDummy.from(pokemon)
                .map(dummy -> {
                    EGGS_CACHE.put(uuid, dummy);
                    return (Pokemon) dummy;
                })
                .orElse(pokemon);
    }

    public static void invalidate(UUID uuid) {
        EGGS_CACHE.invalidate(uuid);
    }
}