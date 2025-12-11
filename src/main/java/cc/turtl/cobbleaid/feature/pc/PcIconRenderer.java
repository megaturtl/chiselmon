package cc.turtl.cobbleaid.feature.pc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.property.PokemonProperties;
import cc.turtl.cobbleaid.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class PcIconRenderer {
    private static final ResourceLocation HIDDEN_ABILITY_ICON = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/ability_patch_icon.png");

    private static final ResourceLocation HIGH_IVS_ICON = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/bottle_cap_icon.png");

    private static final ResourceLocation SIZE_ICON = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/size_shroom_icon.png");

    private static final ResourceLocation SHINY_ICON = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/shiny_sparkle_icon.png");

    private static final ResourceLocation RIDEABLE_ICON = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/saddle_icon.png");

    private static final int ICON_SIZE = 9; // Source texture size
    private static final int ICON_RENDER_SIZE = 5; // Target rendered size
    private static final int ICON_GAP = 0;
    private static final int START_Y = 6;

    private PcIconRenderer() {
    }

    public static void renderIconElements(GuiGraphics context, Pokemon pokemon, int posX, int posY) {
        if (pokemon == null) {
            return;
        }

        ModConfig config = CobbleAid.getInstance().getConfig();
        if (config == null) {
            return;
        }

        int currentY = posY + START_Y;

        // 1. Hidden Ability Icon
        if (config.showHiddenAbilityIcons && PokemonProperties.HAS_HIDDEN_ABILITY.matches(pokemon)) {
            renderIcon(context, HIDDEN_ABILITY_ICON, posX + 1, currentY);
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // 2. High IVs Icon
        if (config.showMaxIvsIcons && PokemonProperties.HAS_HIGH_IVS.matches(pokemon)) {
            renderIcon(context, HIGH_IVS_ICON, posX + 1, currentY);
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // 3. Extreme Size Icon
        if (config.showExtremeSizeIcons && PokemonProperties.IS_EXTREME_SIZE.matches(pokemon)) {
            renderIcon(context, SIZE_ICON, posX + 1, currentY);
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // 4. Shiny Icon
        if (config.showShinyIcons && PokemonProperties.IS_SHINY.matches(pokemon)) {
            renderIcon(context, SHINY_ICON, posX + 1, currentY);
        }

        // 5. Rideable Icon
        if (config.showRideableIcons && PokemonProperties.IS_RIDEABLE.matches(pokemon)) {
            renderIcon(context, RIDEABLE_ICON, posX + 1, currentY);
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