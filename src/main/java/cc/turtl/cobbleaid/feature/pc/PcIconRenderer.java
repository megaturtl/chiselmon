package cc.turtl.cobbleaid.feature.pc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.ModConfig;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

import static cc.turtl.cobbleaid.util.MiscUtils.modResource;

public class PcIconRenderer {
    private static final ResourceLocation HIDDEN_ABILITY_ICON = modResource("textures/gui/pc/ability_patch_icon.png");
    private static final ResourceLocation HIGH_IVS_ICON = modResource("textures/gui/pc/bottle_cap_icon.png");
    private static final ResourceLocation SIZE_ICON = modResource("textures/gui/pc/size_shroom_icon.png");
    private static final ResourceLocation SHINY_ICON = modResource("textures/gui/pc/shiny_sparkle_icon.png");
    private static final ResourceLocation RIDEABLE_ICON = modResource("textures/gui/pc/saddle_icon.png");

    private static final int ICON_SIZE = 9; // Source texture size
    private static final int ICON_RENDER_SIZE = 5; // Target rendered size
    private static final int ICON_GAP = 0;
    private static final int START_Y = 6;
    private static final int ICONS_PER_COLUMN = 3;
    private static final int COLUMN_X_OFFSET = 20;

    /**
     * Helper class to encapsulate icon rendering configuration
     */
    private static class IconConfig {
        final Function<ModConfig.PcConfig.PcIconConfig, Boolean> configGetter;
        final Predicate<Pokemon> predicate;
        final ResourceLocation icon;

        IconConfig(Function<ModConfig.PcConfig.PcIconConfig, Boolean> configGetter,
                Predicate<Pokemon> predicate,
                ResourceLocation icon) {
            this.configGetter = configGetter;
            this.predicate = predicate;
            this.icon = icon;
        }

        boolean shouldRender(ModConfig.PcConfig.PcIconConfig iconConfig, Pokemon pokemon) {
            return configGetter.apply(iconConfig) && predicate.test(pokemon);
        }
    }

    private static final IconConfig[] ICON_CONFIGS = {
            new IconConfig(c -> c.hiddenAbility, PokemonPredicates.HAS_HIDDEN_ABILITY, HIDDEN_ABILITY_ICON),
            new IconConfig(c -> c.highIvs, PokemonPredicates.HAS_HIGH_IVS, HIGH_IVS_ICON),
            new IconConfig(c -> c.extremeSize, PokemonPredicates.IS_EXTREME_SIZE, SIZE_ICON),
            new IconConfig(c -> c.shiny, PokemonPredicates.IS_SHINY, SHINY_ICON),
            new IconConfig(c -> c.rideable, PokemonPredicates.IS_RIDEABLE, RIDEABLE_ICON)
    };

    private PcIconRenderer() {
    }

    public static void renderIconElements(GuiGraphics context, Pokemon pokemon, int posX, int posY) {
        if (pokemon == null) {
            return;
        }

        ModConfig config = CobbleAid.services().config().get();
        if (config == null) {
            return;
        }

        ModConfig.PcConfig.PcIconConfig iconConfig = config.pc.icons;
        int iconIndex = 0;

        for (IconConfig iconCfg : ICON_CONFIGS) {
            if (iconCfg.shouldRender(iconConfig, pokemon)) {
                int column = iconIndex / ICONS_PER_COLUMN;
                int row = iconIndex % ICONS_PER_COLUMN;

                int x = posX + 1 + (column * COLUMN_X_OFFSET);
                int y = posY + START_Y + (row * (ICON_RENDER_SIZE + ICON_GAP));

                renderIcon(context, iconCfg.icon, x, y);
                iconIndex++;
            }
        }
    }

    private static void renderIcon(GuiGraphics context, ResourceLocation icon, int x, int y) {
        final double Z_INDEX_MAX = 99.0;

        int targetSize = ICON_RENDER_SIZE;

        int sourceU = 0;
        int sourceV = 0;
        int sourceWidth = ICON_SIZE;
        int sourceHeight = ICON_SIZE;

        context.pose().pushPose();
        context.pose().translate(0.0, 0.0, Z_INDEX_MAX);

        context.blit(
                icon,
                x, // Target X
                y, // Target Y
                targetSize, // Target Width (5)
                targetSize, // Target Height (5)
                sourceU,
                sourceV,
                sourceWidth,
                sourceHeight,
                ICON_SIZE, // Full Texture Width (9)
                ICON_SIZE // Full Texture Height (9)
        );

        context.pose().popPose();
    }
}