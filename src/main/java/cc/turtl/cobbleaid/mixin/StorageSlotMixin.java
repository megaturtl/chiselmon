package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.gui.pc.PcEggRenderer;
import cc.turtl.cobbleaid.feature.gui.pc.PcIconRenderer;
import cc.turtl.cobbleaid.feature.gui.pc.StorageSlotTooltipState;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEggData;
import net.minecraft.client.gui.GuiGraphics;

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

            Pokemon eggDummyPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();
            PcIconRenderer.renderIconElements(context, eggDummyPokemon, posX, posY);
        } else {
            PcIconRenderer.renderIconElements(context, pokemon, posX, posY);
        }
    }

    // Track which slot is hovered
    @Inject(method = "renderWidget", at = @At("HEAD"), remap = false)
    private void cobbleaid$trackHover(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (config.modDisabled || !config.showTooltips) {
            return;
        }
        
        if (this.isHovered(mouseX, mouseY) && this.getPokemon() != null) {
            StorageSlotTooltipState.setHoveredSlot((StorageSlot)(Object)this, mouseX, mouseY);
        }
    }
}