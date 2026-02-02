package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.config.PCConfig;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Predicate;

public record TooltipEntry(
        Predicate<PCConfig.PCTooltipConfig> configCheck,
        Predicate<Pokemon> pokemonCheck,
        String translationKey,
        Function<Pokemon, Object> valueProvider
) {
    public boolean shouldDisplay(PCConfig.PCTooltipConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }

    public Component getComponent(Pokemon pokemon) {
        Object value = valueProvider.apply(pokemon);
        return ComponentUtils.labelled(
                ComponentUtils.modTranslatable("ui.label." + translationKey),
                value
        );
    }
}