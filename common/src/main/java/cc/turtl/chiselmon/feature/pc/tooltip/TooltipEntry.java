package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.api.OLDPCConfig;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Predicate;

public record TooltipEntry(
        String translationKey,
        Predicate<OLDPCConfig.PCTooltipConfig> configCheck,
        Predicate<Pokemon> pokemonCheck,
        Function<Pokemon, Object> componentProvider
) {
    public boolean shouldDisplay(OLDPCConfig.PCTooltipConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }

    public Component getComponent(Pokemon pokemon) {
        Object value = componentProvider.apply(pokemon);
        return ComponentUtils.labelled(
                ComponentUtils.modTranslatable("ui.label." + translationKey),
                value
        );
    }
}