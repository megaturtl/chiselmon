package cc.turtl.chiselmon.feature.eggpreview;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.config.ModConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NeoDaycareEggCache {

    private static final Cache<UUID, NeoDaycareEggDummy> EGGS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    /**
     * Returns either the egg dummy or the original Pokemon.
     * The dummy is cached but will automatically refresh if hatch data has changed.
     */
    public static Pokemon getDummyOrOriginal(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }

        try {
            ModConfig config = Chiselmon.services().config().get();
            if (config == null || Chiselmon.isDisabled() || !config.eggPreview.enabled) {
                return pokemon;
            }

            if (!NeoDaycareEggDummy.isEgg(pokemon)) {
                return pokemon;
            }

            UUID uuid = pokemon.getUuid();
            if (uuid == null) {
                Chiselmon.getLogger().warn("Pokemon has null UUID, cannot create egg dummy");
                return pokemon;
            }

            NeoDaycareEggDummy existingEgg = EGGS_CACHE.getIfPresent(uuid);
            if (existingEgg != null) {
                existingEgg.updateHatchProgress(pokemon);
                return existingEgg;
            }

            // Create new dummy and cache it
            try {
                NeoDaycareEggDummy newEgg = NeoDaycareEggDummy.createEggFrom(pokemon);
                EGGS_CACHE.put(uuid, newEgg);
                return newEgg;
            } catch (IllegalStateException e) {
                // Egg data is invalid/corrupt, return original
                Chiselmon.getLogger().error("Failed to create egg dummy for {}: {}", uuid, e.getMessage());
                return pokemon;
            }

        } catch (Exception e) {
            Chiselmon.getLogger().error("Unexpected error in getDummyOrOriginal", e);
            return pokemon;
        }
    }

    public static void clearCache() {
        EGGS_CACHE.invalidateAll();
    }

    public static void removeCached(UUID pokemonUuid) {
        if (pokemonUuid != null) {
            EGGS_CACHE.invalidate(pokemonUuid);
        }
    }

    public static long getCacheSize() {
        return EGGS_CACHE.size();
    }
}