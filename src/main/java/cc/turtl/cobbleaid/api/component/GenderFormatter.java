package cc.turtl.cobbleaid.api.component;

import com.cobblemon.mod.common.pokemon.Gender;

import net.minecraft.network.chat.Component;

public final class GenderFormatter {
    private GenderFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("?").withColor(ComponentColor.DARK_GRAY);
    private static final Component MALE = Component.literal("♂").withColor(ComponentColor.BLUE);
    private static final Component FEMALE = Component.literal("♀").withColor(ComponentColor.PINK);
    private static final Component GENDERLESS = Component.literal("●").withColor(ComponentColor.LIGHT_GRAY);

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