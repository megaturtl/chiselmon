package cc.turtl.cobbleaid.api.component;

public class ComponentColor {
    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;
    public static final int LIGHT_GRAY = 0xAAAAAA;
    public static final int DARK_GRAY = 0x555555;

    public static final int RED = 0xF82553;
    public static final int ORANGE = 0xFB6640;
    public static final int YELLOW = 0xF8C421;
    public static final int GREEN = 0x49CC5C;
    public static final int BLUE = 0x2C7CE5;
    public static final int PURPLE = 0x7334E9;
    public static final int PINK = 0xE62CAB;

    // The following colours are from https://bulbapedia.bulbagarden.net/wiki/Egg_Group
    public static final int EGG_MONSTER = 0x97724C;
    public static final int EGG_WATER_1 = 0x6BD1F9;
    public static final int EGG_BUG = 0xAAC22A;
    public static final int EGG_FLYING = 0x90AFF1;
    public static final int EGG_FIELD = 0xE5BA65;
    public static final int EGG_FAIRY = 0xFF9EB9;
    public static final int EGG_GRASS = 0x82D25A;
    public static final int EGG_HUMAN_LIKE = 0x47B7AE;
    public static final int EGG_WATER_3 = 0x2271B4;
    public static final int EGG_MINERAL = 0x979067;
    public static final int EGG_AMORPHOUS = 0x9F82CC;
    public static final int EGG_WATER_2 = 0x4B94ED;
    public static final int EGG_DITTO = 0xB6AAD5;
    public static final int EGG_DRAGON = 0x5E57BF;
    public static final int EGG_UNDISCOVERED = DARK_GRAY;

    public static final int CRIMSON = 0xDC143C;
    public static final int CORAL = 0xFF7F50;
    public static final int GOLD = 0xFFD700;
    public static final int LIME = 0x32CD32;
    public static final int CYAN = 0x00CED1;
    public static final int INDIGO = 0x4B0082;
    public static final int MAGENTA = 0xFF00FF;
    public static final int BROWN = 0x8B4513;
    public static final int TURQUOISE = 0x40E0D0;
    public static final int LAVENDER = 0xE6E6FA;
    public static final int MINT = 0x98FF98;
    public static final int PEACH = 0xFFDAB9;
    public static final int NAVY = 0x000080;
    public static final int MAROON = 0x800000;
    public static final int OLIVE = 0x808000;
    public static final int TEAL = 0x008080;

    public int alphaColor(int color, double opacity) {
        int alphaValue = percentToAlpha(opacity);
        int alphaShifted = alphaValue << 24;
        return alphaShifted | color;
    }

    private static int percentToAlpha(double opacity) {
        double calculatedAlpha = opacity * 255.0;
        int roundedAlpha = (int) Math.round(calculatedAlpha);
        return Math.min(255, roundedAlpha);
    }
}