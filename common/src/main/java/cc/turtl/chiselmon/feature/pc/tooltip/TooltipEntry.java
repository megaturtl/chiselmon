package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.client.config.category.PCConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

import java.util.function.Function;
import java.util.function.Predicate;

import static cc.turtl.chiselmon.core.util.format.ComponentUtilsKt.labelled;

public record TooltipEntry(
        String translationKey,
        Predicate<PCConfig.TooltipConfig> configCheck,
        Predicate<Pokemon> pokemonCheck,
        Function<Pokemon, Object> componentProvider
) {
    public boolean shouldDisplay(PCConfig.TooltipConfig config, Pokemon pokemon) {
        return configCheck.test(config) && pokemonCheck.test(pokemon);
    }

    public Component getComponent(Pokemon pokemon) {
        Object value = componentProvider.apply(pokemon);
        return labelled(
                Component.translatable("chiselmon.ui.label." + translationKey),
                value);
    }
}