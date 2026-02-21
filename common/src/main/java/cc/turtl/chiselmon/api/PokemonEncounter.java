package cc.turtl.chiselmon.api;

import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.UUID;

/**
 * Immutable snapshot of a single pokemon's state when first encountered.
 */
public record PokemonEncounter(
        long encounteredAtMs,
        UUID uuid,
        Species species,
        FormData form,
        int level,
        float scaleModifier,
        boolean isShiny,
        boolean isLegendary,
        boolean isExtremeSize,
        boolean spawnedFromSnack,
        int playerX,
        int playerY,
        int playerZ,
        int pokemonX,
        int pokemonY,
        int pokemonZ,
        String dimension,
        String biome
) {

    /**
     * Creates a PokemonEncounter snapshot from a live PokemonEntity.
     *
     * @param pe The PokemonEntity to capture
     * @return A new immutable encounter record
     */
    public static PokemonEncounter from(PokemonEntity pe) {
        Pokemon pokemon = pe.getPokemon();
        LocalPlayer player = Minecraft.getInstance().player;

        return new PokemonEncounter(
                System.currentTimeMillis(),
                pe.getUUID(),
                pokemon.getSpecies(),
                pe.getForm(),
                pokemon.getLevel(),
                pokemon.getScaleModifier(),
                PokemonPredicates.IS_SHINY.test(pokemon),
                PokemonPredicates.IS_LEGENDARY.test(pokemon),
                PokemonPredicates.IS_EXTREME_SIZE.test(pokemon),
                PokemonEntityPredicates.FROM_POKESNACK.test(pe),
                player != null ? player.getBlockX() : 1,
                player != null ? player.getBlockY() : 1,
                player != null ? player.getBlockZ() : 1,
                pe.getBlockX(),
                pe.getBlockY(),
                pe.getBlockZ(),
                pe.level().dimension().location().toString().intern(),
                pe.level().getBiome(pe.blockPosition()).getRegisteredName().intern());
    }
}