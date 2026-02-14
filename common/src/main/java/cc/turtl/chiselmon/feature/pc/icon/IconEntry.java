package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.api.OLDPCConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

public record IconEntry(
        ResourceLocation resource,
        Predicate<OLDPCConfig.PcIconConfig> configCheck,
        Predicate<Pokemon> pokemonCheck
) {
    public boolean shouldDisplay(OLDPCConfig.PcIconConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }
}