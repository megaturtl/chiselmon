package cc.turtl.cobbleaid.api;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.util.ColorUtil;
import cc.turtl.cobbleaid.api.util.IVsUtil;
import cc.turtl.cobbleaid.api.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PokemonTooltips {
    private static final MutableComponent UNKNOWN = Component.literal("???").withStyle(ChatFormatting.DARK_GRAY);

    private static Component createLabeledTooltip(String label, MutableComponent value) {
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

        MutableComponent valueComponent = Component.literal(sizeString)
                .withStyle(ChatFormatting.DARK_AQUA);

        return createLabeledTooltip("Size", valueComponent);
    }

    public static Component computeGenderTooltip(Pokemon pokemon) {
        Gender gender = pokemon != null ? pokemon.getGender() : null;

        MutableComponent valueComponent;
        if (gender == null) {
            valueComponent = UNKNOWN;
        } else {
            switch (gender) {
                case MALE:
                    valueComponent = Component.literal("Male").withStyle(ChatFormatting.AQUA);
                    break;
                case FEMALE:
                    valueComponent = Component.literal("Female").withStyle(ChatFormatting.LIGHT_PURPLE);
                    break;
                case GENDERLESS:
                    valueComponent = Component.literal("Enby").withStyle(ChatFormatting.GRAY);
                    break;
                default:
                    valueComponent = UNKNOWN;
                    break;
            }
        }

        return createLabeledTooltip("Gender", valueComponent);
    }

    public static Component computeTypingTooltip(Pokemon pokemon) {
        ElementalType primaryType = pokemon != null ? pokemon.getPrimaryType() : null;
        ElementalType secondaryType = pokemon != null ? pokemon.getSecondaryType() : null;

        if (primaryType == null || primaryType.getDisplayName() == null) {
            return createLabeledTooltip("Type", UNKNOWN);
        }

        MutableComponent valueComponent = (primaryType.getDisplayName().withColor(primaryType.getHue()));

        if (secondaryType != null) {
            valueComponent
            .append(" / ").withStyle(ChatFormatting.WHITE)
            .append(secondaryType.getDisplayName().withColor(secondaryType.getHue()));
        }

        return createLabeledTooltip("Type", valueComponent);
    }

    public static Component computeNatureTooltip(Pokemon pokemon) {
        Nature nature = pokemon != null ? pokemon.getEffectiveNature() : null;

        if (nature == null || nature.getDisplayName() == null) {
            return createLabeledTooltip("Nature", UNKNOWN);
        }

        MutableComponent valueComponent = Component.translatable(nature.getDisplayName())
                .withStyle(ChatFormatting.GRAY);

        return createLabeledTooltip("Nature", valueComponent);
    }

    public static Component computeIVsTooltip(Pokemon pokemon) {
        IVs iVs = pokemon != null ? pokemon.getIvs() : null;

        if (iVs == null) {
            return createLabeledTooltip("IVs", UNKNOWN);
        }

        MutableComponent valueComponent = IVsUtil.getIvsComponent(iVs);
        return createLabeledTooltip("IVs", valueComponent);
    }

    public static Component computeCatchChanceTooltip(PokemonEntity pokemonEntity, Player player) {
        if (player == null || pokemonEntity == null) {
            return createLabeledTooltip("Catch Chance:", UNKNOWN);
        }

        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem == null || !(mainHandItem.getItem() instanceof PokeBallItem pokeBallItem)) {
            return createLabeledTooltip("Catch Chance:", UNKNOWN);
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

            MutableComponent valueComponent = Component.literal(minChanceString).withColor(minRgb)
                    .append(Component.literal(" - "))
                    .append(Component.literal(maxChanceString).withColor(maxRgb));
            
            return createLabeledTooltip("Catch Chance:", valueComponent);
        } catch (Exception e) {
            return createLabeledTooltip("Catch Chance:", UNKNOWN);
        }
    }
}