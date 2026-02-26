package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.config.category.PCConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

public record IconEntry(
        ResourceLocation resource,
        Predicate<PCConfig.IconConfig> configCheck,
        Predicate<Pokemon> pokemonCheck
) {
    public boolean shouldDisplay(PCConfig.IconConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }
}