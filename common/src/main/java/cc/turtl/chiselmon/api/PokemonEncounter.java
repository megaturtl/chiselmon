package cc.turtl.chiselmon.api;

import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Immutable snapshot of a single pokemon's state when first encountered.
 */
public record PokemonEncounter(
        long encounteredMs,

        // Identity
        UUID uuid,
        Species species,
        FormData form,

        // Attributes
        int level,
        Gender gender,
        float scale,

        // Flags
        boolean isShiny,
        boolean isLegendary,

        // World context
        String dimension,
        String biome,
        long dayTime,
        boolean isRaining,

        // Block context
        String blockName,

        // Snack
        boolean spawnedFromSnack,

        // Positions
        int pokemonX,
        int pokemonY,
        int pokemonZ,
        int playerX,
        int playerY,
        int playerZ
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
        BlockState blockState = pe.getBlockStateOn();

        return new PokemonEncounter(
                System.currentTimeMillis(),

                // Identity
                pe.getUUID(),
                pokemon.getSpecies(),
                pe.getForm(),

                // Attributes
                pokemon.getLevel(),
                pokemon.getGender(),
                pokemon.getScaleModifier(),

                // Flags
                PokemonPredicates.IS_SHINY.test(pokemon),
                PokemonPredicates.IS_LEGENDARY.test(pokemon),

                // World context
                pe.level().dimension().location().toString().intern(),
                pe.level().getBiome(pe.blockPosition()).getRegisteredName().intern(),
                pe.level().getDayTime() % 24000,
                pe.level().isRaining(),
                BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString(),

                // Snack
                PokemonEntityPredicates.FROM_POKESNACK.test(pe),

                // Positions
                pe.getBlockX(),
                pe.getBlockY(),
                pe.getBlockZ(),
                player != null ? player.getBlockX() : 0,
                player != null ? player.getBlockY() : 0,
                player != null ? player.getBlockZ() : 0
        );
    }
}