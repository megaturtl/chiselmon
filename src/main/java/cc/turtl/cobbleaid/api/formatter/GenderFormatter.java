package cc.turtl.cobbleaid.api.formatter;

import com.cobblemon.mod.common.pokemon.Gender;

import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.network.chat.Component;

public final class GenderFormatter {
    private GenderFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("?").withColor(ColorUtil.DARK_GRAY);
    private static final Component MALE = Component.literal("♂").withColor(ColorUtil.BLUE);
    private static final Component FEMALE = Component.literal("♀").withColor(ColorUtil.PINK);
    private static final Component GENDERLESS = Component.literal("●").withColor(ColorUtil.LIGHT_GRAY);

    public static Component formatSymbol(Gender gender) {
        if (gender == null)
            return UNKNOWN;

        return switch (gender) {
            case MALE -> MALE;
            case FEMALE -> FEMALE;
            case GENDERLESS -> GENDERLESS;
        };
    }
}