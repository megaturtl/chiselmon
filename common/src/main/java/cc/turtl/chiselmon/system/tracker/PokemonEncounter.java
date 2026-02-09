package cc.turtl.chiselmon.system.tracker;

import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;

/**
 * Immutable snapshot of a Pokemon's state when first encountered.
 */
public record PokemonEncounter(
        Species species,
        FormData form,
        int level,
        float scaleModifier,
        boolean isShiny,
        boolean isLegendary,
        boolean isExtremeSize,
        boolean spawnedFromSnack,
        int blockX,
        int blockY,
        int blockZ,
        String dimension,
        String biome,
        long discoveredAtMillis) {

    /**
     * Creates a PokemonEncounter snapshot from a live PokemonEntity.
     *
     * @param pe The PokemonEntity to capture
     * @return A new immutable encounter record
     */
    public static PokemonEncounter from(PokemonEntity pe) {
        Pokemon pokemon = pe.getPokemon();

        return new PokemonEncounter(
                pokemon.getSpecies(),
                pe.getForm(),
                pokemon.getLevel(),
                pokemon.getScaleModifier(),
                PokemonPredicates.IS_SHINY.test(pokemon),
                PokemonPredicates.IS_LEGENDARY.test(pokemon),
                PokemonPredicates.IS_EXTREME_SIZE.test(pokemon),
                PokemonEntityPredicates.FROM_POKESNACK.test(pe),
                pe.getBlockX(),
                pe.getBlockY(),
                pe.getBlockZ(),
                pe.level().dimension().location().toString().intern(),
                pe.level().getBiome(pe.blockPosition()).getRegisteredName().intern(),
                System.currentTimeMillis());
    }
}