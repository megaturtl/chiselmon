package cc.turtl.cobbleaid.api.component;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Species;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class EggGroupFormatter {
    private EggGroupFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ComponentColor.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal(" / ").withColor(ComponentColor.LIGHT_GRAY);

    private static int getEggGroupColor(EggGroup group) {
        return switch (group) {
            case MONSTER -> ComponentColor.EGG_MONSTER;
            case WATER_1 -> ComponentColor.EGG_WATER_1;
            case BUG -> ComponentColor.EGG_BUG;
            case FLYING -> ComponentColor.EGG_FLYING;
            case FIELD -> ComponentColor.EGG_FIELD;
            case FAIRY -> ComponentColor.EGG_FAIRY;
            case GRASS -> ComponentColor.EGG_GRASS;
            case HUMAN_LIKE -> ComponentColor.EGG_HUMAN_LIKE;
            case WATER_3 -> ComponentColor.EGG_WATER_3;
            case MINERAL -> ComponentColor.EGG_MINERAL;
            case AMORPHOUS -> ComponentColor.EGG_AMORPHOUS;
            case WATER_2 -> ComponentColor.EGG_WATER_2;
            case DITTO -> ComponentColor.EGG_DITTO;
            case DRAGON -> ComponentColor.EGG_DRAGON;
            case UNDISCOVERED -> ComponentColor.EGG_UNDISCOVERED;
            default -> ComponentColor.WHITE;
        };
    }

    public static Component format(Species species) {
        if (species.getEggGroups().isEmpty()) {
            return UNKNOWN;
        }

        MutableComponent result = Component.empty();
        boolean first = true;

        for (EggGroup group : species.getEggGroups()) {
            if (!first)
                result.append(SEPARATOR);
            result.append(Component.literal(group.getShowdownID()).withColor(getEggGroupColor(group)));
            first = false;
        }

        return result;
    }
}