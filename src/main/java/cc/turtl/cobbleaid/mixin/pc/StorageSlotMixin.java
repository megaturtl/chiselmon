package cc.turtl.cobbleaid.mixin.pc;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.PcIconRenderer;
import cc.turtl.cobbleaid.feature.pc.neodaycare.PcEggRenderer;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareDummyPokemon;
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
    private void cobbleaid$renderCustomElements(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        if (config.modDisabled) {
            return;
        }
        Pokemon pokemon = getPokemon();

        if (config.pc.showEggPreview && pokemon instanceof NeoDaycareDummyPokemon) {
            PcEggRenderer.renderEggPreviewElements(context, (NeoDaycareDummyPokemon) pokemon, posX, posY);
        }

        PcIconRenderer.renderIconElements(context, pokemon, posX, posY);
    }
}