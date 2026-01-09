package cc.turtl.chiselmon.compat.neodaycare;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.config.ModConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NeoDaycareEggManager {

    // Cache of dummy Pokemon by original Pokemon UUID
    private static final Cache<UUID, NeoDaycareEggDummy> EGGS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    public static Pokemon getDummyOrOriginal(Pokemon pokemon) {
        if (pokemon == null)
            return null;

        ModConfig config = Chiselmon.services().config().get();
        if (Chiselmon.isDisabled() || !config.pc.showEggPreview || !NeoDaycareEggDummy.isEgg(pokemon)) {
            return pokemon;
        }

        UUID uuid = pokemon.getUuid();

        NeoDaycareEggDummy existingEgg = EGGS_CACHE.getIfPresent(uuid);

        if (existingEgg != null) {
            return existingEgg;
        }

        // Otherwise, create/re-create the dummy and update the cache
        NeoDaycareEggDummy newEgg = NeoDaycareEggDummy.createEggFrom(pokemon);

        EGGS_CACHE.put(uuid, newEgg);
        return newEgg;
    }

    public static void clearCache() {
        EGGS_CACHE.invalidateAll();
    }

    public static void removeCached(UUID pokemonUuid) {
        EGGS_CACHE.invalidate(pokemonUuid);
    }
}
