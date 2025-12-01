package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.PokemonTooltips;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.gui.pc.PcEggRenderer;
import cc.turtl.cobbleaid.feature.gui.pc.PcIconRenderer;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEggData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow(remap = false)
    public abstract boolean isHovered(int mouseX, int mouseY);

    ModConfig config = CobbleAid.getInstance().getConfig();

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1), remap = false)
    private void cobbleaid$renderCustomFeatures(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        if (config.modDisabled) {
            return;
        }
        Pokemon pokemon = getPokemon();

        if (config.showEggPreview != false && NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
            PcEggRenderer.renderEggPreviewElements(context, pokemon, posX, posY, delta);

            // If an egg, use the potential data to render icons
            Pokemon eggDummyPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();
            PcIconRenderer.renderIconElements(context, eggDummyPokemon, posX, posY);
        } else {
            PcIconRenderer.renderIconElements(context, pokemon, posX, posY);
        }
    }

    // Render a hover tooltip for useful info
    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void cobbleaid$renderTooltip(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (config.modDisabled || !config.showTooltips) {
            return;
        }
        Pokemon pokemon = this.getPokemon();
        final Pokemon tooltipPokemon;

        if (pokemon == null) {
            return;
        }

        if (NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
            tooltipPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();
        } else {
            tooltipPokemon = pokemon;
        }

        if (tooltipPokemon != null && this.isHovered(mouseX, mouseY)) {
            List<Component> tooltip = new ArrayList<>();

            context.pose().pushPose();
            context.pose().translate(0, 0, 1000.0);

            tooltip.add(PokemonTooltips.computeSizeTooltip(tooltipPokemon));
            if (config.showDetailedTooltipOnShift && Screen.hasShiftDown()) {
                tooltip.add(PokemonTooltips.computeIVsTooltip(tooltipPokemon));
                tooltip.add(Component.literal("§dOT: §f" + tooltipPokemon.getOriginalTrainerName()));
                tooltip.add(Component.literal("§dFriendship: §f" + tooltipPokemon.getFriendship()));
                tooltip.add(Component.literal("§dMarks: §f" + tooltipPokemon.getMarks().size()));
                tooltip.add(Component.literal("§dForm: §f" + tooltipPokemon.getForm().getName()));
            }

            context.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    tooltip,
                    mouseX,
                    mouseY);

            context.pose().popPose();
        }
    }
}