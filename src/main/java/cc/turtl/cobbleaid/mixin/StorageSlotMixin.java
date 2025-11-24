package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.filter.PokemonFilterCondition;
import cc.turtl.cobbleaid.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {

    private static final ResourceLocation hiddenAbilityIcon = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/ability_patch_icon.png");

    private static final ResourceLocation highIvsIcon = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/bottle_cap_icon.png");
    private static final ResourceLocation sizeIcon = ResourceLocation.fromNamespaceAndPath(
        "cobbleaid",
        "textures/gui/pc/size_shroom_icon.png");

    private static final ResourceLocation shinyIcon = ResourceLocation.fromNamespaceAndPath(
            "cobbleaid",
            "textures/gui/pc/shiny_sparkle_icon.png");

    private static final int ICON_SIZE = 9;
    private static final int ICON_RENDER_SIZE = 5;
    private static final int ICON_GAP = 0;
    private static final int START_Y = 6;

    @Shadow
    public abstract Pokemon getPokemon();

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1), remap = false)
    private void cobbleaid$renderIcons(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        Pokemon pokemon = getPokemon();
        if (pokemon == null) {
            return;
        }

        ModConfig config = CobbleAid.getInstance().getConfig();
        if (config == null) {
            return;
        }

        int currentY = posY + START_Y;

        // Check and render hidden ability icon first
        if (config.pcConfig.showHiddenAbility && PokemonFilterCondition.hasHiddenAbility.matches(pokemon)) {
            renderIcon(context, hiddenAbilityIcon, posX + 1, currentY); // Pass 'context'
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // Check and render high IVs icon second
        if (config.pcConfig.showHighIvs && PokemonFilterCondition.hasHighIVs.matches(pokemon)) {
            renderIcon(context, highIvsIcon, posX + 1, currentY); // Pass 'context'
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // Check and render high IVs icon third
        if (config.pcConfig.showHighIvs && PokemonFilterCondition.isExtremeSize.matches(pokemon)) {
            renderIcon(context, sizeIcon, posX + 1, currentY); // Pass 'context'
            currentY += ICON_RENDER_SIZE + ICON_GAP;
        }

        // Check and render shiny icon fourth
        if (config.pcConfig.showShiny && pokemon.getShiny()) {
            renderIcon(context, shinyIcon, posX + 1, currentY); // Pass 'context'
        }
    }

    private void renderIcon(GuiGraphics context, ResourceLocation icon, int x, int y) {

        // Calculate the target size in pixels
        int targetSize = ICON_RENDER_SIZE;

        // Calculate the source texture UV coordinates (0, 0 to 9, 9)
        int sourceU = 0;
        int sourceV = 0;
        int sourceWidth = ICON_SIZE;
        int sourceHeight = ICON_SIZE;

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
    }
}