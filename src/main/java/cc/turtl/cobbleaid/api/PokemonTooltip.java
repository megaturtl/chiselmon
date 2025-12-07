package cc.turtl.cobbleaid.api;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.component.GenderFormatter;
import cc.turtl.cobbleaid.api.component.IVsFormatter;
import cc.turtl.cobbleaid.api.component.TypingFormatter;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import cc.turtl.cobbleaid.api.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PokemonTooltip {
    private static final Component UNKNOWN = Component.literal("???").withStyle(ChatFormatting.DARK_GRAY);

    private static Component createLabeledTooltip(String label, Component value) {
        return Component.literal(label + ": ")
                .withStyle(ChatFormatting.WHITE)
                .append(value);
    }

    public static Component computeNameTooltip(Pokemon pokemon) {
        if (pokemon == null || pokemon.getSpecies() == null) {
            return UNKNOWN;
        }

        int level = pokemon.getLevel();
        String species = pokemon.getSpecies().getName();

        return Component.literal(species + " (Lvl. " + level + ")")
                .withStyle(ChatFormatting.WHITE);
    }

    public static Component computeSizeTooltip(Pokemon pokemon) {
        if (pokemon == null) {
            return createLabeledTooltip("Size", UNKNOWN);
        }

        float size = pokemon.getScaleModifier();
        String sizeString = String.format("%.2f", size);

        Component valueComponent = Component.literal(sizeString)
                .withStyle(ChatFormatting.DARK_AQUA);

        return createLabeledTooltip("Size", valueComponent);
    }

    public static Component computeGenderTooltip(Pokemon pokemon) {
        return GenderFormatter.formatSymbolLabeled(pokemon != null ? pokemon.getGender() : null);
    }

    public static Component computeTypingTooltip(Pokemon pokemon) {
        return TypingFormatter.formatLabeled(
                pokemon != null ? pokemon.getPrimaryType() : null,
                pokemon != null ? pokemon.getSecondaryType() : null);
    }

    public static Component computeNatureTooltip(Pokemon pokemon) {
        Nature nature = pokemon != null ? pokemon.getEffectiveNature() : null;

        if (nature == null || nature.getDisplayName() == null) {
            return createLabeledTooltip("Nature", UNKNOWN);
        }

        Component valueComponent = Component.translatable(nature.getDisplayName())
                .withStyle(ChatFormatting.GRAY);

        return createLabeledTooltip("Nature", valueComponent);
    }

    public static Component computeIVsTooltip(Pokemon pokemon) {
        return IVsFormatter.formatLabeled(pokemon != null ? pokemon.getIvs() : null);
    }

    public static Component computeCatchChanceTooltip(PokemonEntity pokemonEntity, Player player) {
        if (player == null || pokemonEntity == null) {
            return createLabeledTooltip("Catch Chance", UNKNOWN);
        }

        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem == null || !(mainHandItem.getItem() instanceof PokeBallItem pokeBallItem)) {
            return createLabeledTooltip("Catch Chance", UNKNOWN);
        }

        try {
            double catchChance = CaptureCalculator.getCatchChance(player, pokemonEntity,
                    pokeBallItem.getPokeBall());
            double maxCatchChance = CaptureCalculator.getMaxCatchChance(player, pokemonEntity,
                    pokeBallItem.getPokeBall());

            String minChanceString = StringUtil.formatPercentage(catchChance);
            String maxChanceString = StringUtil.formatPercentage(maxCatchChance);

            int minRgb = ColorUtil.getRatioGradientColor(catchChance);
            int maxRgb = ColorUtil.getRatioGradientColor(maxCatchChance);

            Component valueComponent = Component.literal(minChanceString).withColor(minRgb)
                    .append(Component.literal(" - "))
                    .append(Component.literal(maxChanceString).withColor(maxRgb));

            return createLabeledTooltip("Catch Chance", valueComponent);
        } catch (Exception e) {
            return createLabeledTooltip("Catch Chance", UNKNOWN);
        }
    }
}