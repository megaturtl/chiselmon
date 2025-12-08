package cc.turtl.cobbleaid.api;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;

import cc.turtl.cobbleaid.api.capture.CaptureChanceEstimator;
import cc.turtl.cobbleaid.api.component.ComponentColor;
import cc.turtl.cobbleaid.api.component.EVYieldFormatter;
import cc.turtl.cobbleaid.api.component.EggGroupFormatter;
import cc.turtl.cobbleaid.api.component.GenderFormatter;
import cc.turtl.cobbleaid.api.component.IVsFormatter;
import cc.turtl.cobbleaid.api.component.SelfDamageFormatter;
import cc.turtl.cobbleaid.api.component.TypingFormatter;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import cc.turtl.cobbleaid.api.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PokemonTooltip {
    private static final Component UNKNOWN = Component.literal("???").withStyle(ChatFormatting.DARK_GRAY);

    public static Component nameTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        final int level = pokemon.getLevel();
        final String speciesName = pokemon.getSpecies().getName();

        float size = pokemon.getScaleModifier();
        String sizeString = String.format("%.2f", size);

        if (speciesName == null || speciesName.isEmpty()) {
            return UNKNOWN;
        }

        final Component genderSymbol = GenderFormatter.formatSymbol(pokemon.getGender());

        MutableComponent tooltip = Component.empty();

        tooltip.append(genderSymbol);
        tooltip.append(Component.literal(" "));
        tooltip.append(Component.literal(speciesName)
                .withColor(ComponentColor.WHITE));
        tooltip.append(Component.literal(" Lv. ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(Component.literal(String.valueOf(level))
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(Component.literal(" (")
                .withColor(ComponentColor.TEAL));
        tooltip.append(Component.literal(sizeString)
                .withColor(ComponentColor.TEAL));
        tooltip.append(Component.literal(")")
                .withColor(ComponentColor.TEAL));

        return tooltip;
    }

    public static Component typingTooltip(Pokemon pokemon) {
        final Component typingComponent = TypingFormatter.format(
                pokemon != null ? pokemon.getPrimaryType() : null,
                pokemon != null ? pokemon.getSecondaryType() : null);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Type: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(typingComponent);

        return tooltip;
    }

    public static Component eggGroupTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        Species species = pokemon.getSpecies();
        final Component eggGroupComponent = EggGroupFormatter.format(species);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Egg Group: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(eggGroupComponent);

        return tooltip;
    }

    public static Component eVYieldTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        Species species = pokemon.getSpecies();
        final Component eVYieldComponent = EVYieldFormatter.format(species);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("EV Yield: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(eVYieldComponent);

        return tooltip;
    }

    public static Component sizeTooltip(Pokemon pokemon) {
        float size = pokemon.getScaleModifier();
        String sizeString = String.format("%.2f", size);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Size: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(Component.literal(sizeString)
                .withColor(ComponentColor.WHITE));

        return tooltip;
    }

    public static Component iVsTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        IVs iVs = pokemon.getIvs();
        final Component iVsComponent = IVsFormatter.format(iVs);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("IVs: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(iVsComponent);

        return tooltip;
    }

    public static Component catchRateTooltip(Pokemon pokemon) {
        Species species = pokemon.getSpecies();

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Catch Rate: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(Component.literal(String.valueOf(species.getCatchRate()))
                .withColor(ComponentColor.WHITE));

        return tooltip;
    }

    public static Component catchChanceTooltip(PokemonEntity pokemonEntity, PokeBall ball) {
        Species species = pokemonEntity.getPokemon().getSpecies();
        float catchChance = CaptureChanceEstimator.estimateCaptureProbability(pokemonEntity, ball);
        int rgb = ColorUtil.getRatioGradientColor((float) catchChance / 1.0f) & 0xFFFFFF;

        Component catchChanceComponent = Component.literal(StringUtil.formatPercentage(catchChance)).withColor(rgb);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Catch Rate: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(Component.literal(String.valueOf(species.getCatchRate()))
                .withColor(ComponentColor.WHITE));
        tooltip.append(Component.literal(" (")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(catchChanceComponent);
        tooltip.append(Component.literal(")")
                .withColor(ComponentColor.LIGHT_GRAY));

        return tooltip;
    }

    public static Component selfDamagingTooltip(Pokemon pokemon) {
        final Component selfDamagingMovesComponent = SelfDamageFormatter.format(pokemon);

        MutableComponent tooltip = Component.empty();

        tooltip.append(Component.literal("Possible Self Damaging Moves: ")
                .withColor(ComponentColor.LIGHT_GRAY));
        tooltip.append(selfDamagingMovesComponent);
        
        return tooltip;        
    }
}