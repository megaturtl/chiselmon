package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.config.PCConfig;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

public record IconEntry(
        ResourceLocation resource,
        Predicate<PCConfig.PcIconConfig> configCheck,
        Predicate<Pokemon> pokemonCheck
) {
    public boolean shouldDisplay(PCConfig.PcIconConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }
}