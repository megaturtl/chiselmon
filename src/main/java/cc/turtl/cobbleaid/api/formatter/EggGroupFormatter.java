package cc.turtl.cobbleaid.api.formatter;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Species;

import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class EggGroupFormatter {
    private EggGroupFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ColorUtil.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal(" / ").withColor(ColorUtil.LIGHT_GRAY);

    // The following colours are from https://bulbapedia.bulbagarden.net/wiki/COLOR_Group
    public static final int COLOR_MONSTER = 0x97724C;
    public static final int COLOR_WATER_1 = 0x6BD1F9;
    public static final int COLOR_BUG = 0xAAC22A;
    public static final int COLOR_FLYING = 0x90AFF1;
    public static final int COLOR_FIELD = 0xE5BA65;
    public static final int COLOR_FAIRY = 0xFF9EB9;
    public static final int COLOR_GRASS = 0x82D25A;
    public static final int COLOR_HUMAN_LIKE = 0x47B7AE;
    public static final int COLOR_WATER_3 = 0x2271B4;
    public static final int COLOR_MINERAL = 0x979067;
    public static final int COLOR_AMORPHOUS = 0x9F82CC;
    public static final int COLOR_WATER_2 = 0x4B94ED;
    public static final int COLOR_DITTO = 0xB6AAD5;
    public static final int COLOR_DRAGON = 0x5E57BF;
    public static final int COLOR_UNDISCOVERED = ColorUtil.DARK_GRAY;

    private static int getEggGroupColor(EggGroup group) {
        return switch (group) {
            case MONSTER -> COLOR_MONSTER;
            case WATER_1 -> COLOR_WATER_1;
            case BUG -> COLOR_BUG;
            case FLYING -> COLOR_FLYING;
            case FIELD -> COLOR_FIELD;
            case FAIRY -> COLOR_FAIRY;
            case GRASS -> COLOR_GRASS;
            case HUMAN_LIKE -> COLOR_HUMAN_LIKE;
            case WATER_3 -> COLOR_WATER_3;
            case MINERAL -> COLOR_MINERAL;
            case AMORPHOUS -> COLOR_AMORPHOUS;
            case WATER_2 -> COLOR_WATER_2;
            case DITTO -> COLOR_DITTO;
            case DRAGON -> COLOR_DRAGON;
            case UNDISCOVERED -> COLOR_UNDISCOVERED;
            default -> ColorUtil.WHITE;
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