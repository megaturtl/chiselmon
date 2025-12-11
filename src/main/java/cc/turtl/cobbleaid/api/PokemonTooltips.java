package cc.turtl.cobbleaid.api;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;

import cc.turtl.cobbleaid.api.capture.CaptureChanceEstimator;
import cc.turtl.cobbleaid.api.formatter.EVYieldFormatter;
import cc.turtl.cobbleaid.api.formatter.EggGroupFormatter;
import cc.turtl.cobbleaid.api.formatter.GenderFormatter;
import cc.turtl.cobbleaid.api.formatter.IVsFormatter;
import cc.turtl.cobbleaid.api.formatter.SelfDamageFormatter;
import cc.turtl.cobbleaid.api.formatter.TypingFormatter;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import cc.turtl.cobbleaid.api.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PokemonTooltips {
    private static final Component UNKNOWN = Component.literal("???").withStyle(ChatFormatting.DARK_GRAY);

    public static Component nameTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        String speciesName = pokemon.getSpecies().getName();
        if (speciesName == null || speciesName.isEmpty()) {
            return UNKNOWN;
        }

        Component genderSymbol = GenderFormatter.formatSymbol(pokemon.getGender());
        String sizeString = String.format("%.2f", pokemon.getScaleModifier());

        return Component.empty()
                .append(genderSymbol)
                .append(Component.literal(" "))
                .append(Component.literal(speciesName).withColor(ColorUtil.WHITE))
                .append(Component.literal(" Lv. ").withColor(ColorUtil.LIGHT_GRAY))
                .append(Component.literal(String.valueOf(pokemon.getLevel())).withColor(ColorUtil.LIGHT_GRAY))
                .append(Component.literal(" (").withColor(ColorUtil.TEAL))
                .append(Component.literal(sizeString).withColor(ColorUtil.TEAL))
                .append(Component.literal(")").withColor(ColorUtil.TEAL));
    }

    public static Component typingTooltip(Pokemon pokemon) {
        Component typingComponent = TypingFormatter.format(
                pokemon != null ? pokemon.getPrimaryType() : null,
                pokemon != null ? pokemon.getSecondaryType() : null);

        return labeledTooltip("Type: ", typingComponent);
    }

    public static Component eggGroupTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        Component eggGroupComponent = EggGroupFormatter.format(pokemon.getSpecies());
        return labeledTooltip("Egg Group: ", eggGroupComponent);
    }

    public static Component eVYieldTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        Component eVYieldComponent = EVYieldFormatter.format(pokemon.getSpecies());
        return labeledTooltip("EV Yield: ", eVYieldComponent);
    }

    public static Component sizeTooltip(Pokemon pokemon) {
        if (pokemon == null) {
            return UNKNOWN;
        }

        String sizeString = String.format("%.2f", pokemon.getScaleModifier());
        return labeledTooltip("Size: ", sizeString);
    }

    public static Component iVsTooltip(Pokemon pokemon) {
        if (pokemon == null) {
            return UNKNOWN;
        }

        Component iVsComponent = IVsFormatter.format(pokemon.getIvs());
        return labeledTooltip("IVs: ", iVsComponent);
    }

    public static Component catchChanceTooltip(PokemonEntity pokemonEntity, PokeBall ball) {
        Species species = pokemonEntity.getPokemon().getSpecies();
        float catchChance = CaptureChanceEstimator.estimateCaptureProbability(pokemonEntity, ball);
        int rgb = ColorUtil.getRatioGradientColor(catchChance / 1.0f) & 0xFFFFFF;

        Component catchChanceComponent = Component.literal(StringUtil.formatPercentage(catchChance)).withColor(rgb);

        return Component.empty()
                .append(Component.literal("Catch Rate: ").withColor(ColorUtil.LIGHT_GRAY))
                .append(Component.literal(String.valueOf(species.getCatchRate())).withColor(ColorUtil.WHITE))
                .append(Component.literal(" (").withColor(ColorUtil.LIGHT_GRAY))
                .append(catchChanceComponent)
                .append(Component.literal(")").withColor(ColorUtil.LIGHT_GRAY));
    }

    public static Component selfDamagingTooltip(Pokemon pokemon) {
        Component selfDamagingMovesComponent = SelfDamageFormatter.format(pokemon);
        return labeledTooltip("Possible Self Damaging Moves: ", selfDamagingMovesComponent);
    }

    /**
     * Creates a styled tooltip component with a label and value.
     */
    public static Component labeledTooltip(@NotNull String label, Object value) {
        MutableComponent tooltip = Component.literal(label).withColor(ColorUtil.LIGHT_GRAY);

        if (value == null) {
            tooltip.append(UNKNOWN);
        } else if (value instanceof Component) {
            tooltip.append((Component) value);
        } else {
            tooltip.append(Component.literal(value.toString()).withColor(ColorUtil.WHITE));
        }

        return tooltip;
    }
}