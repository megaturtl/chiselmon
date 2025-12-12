package cc.turtl.cobbleaid.mixin.pc;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareDummyPokemon;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to add custom rendering to Pokemon PC storage slots.
 * This mixin is kept minimal - it just hooks into the render method
 * and delegates to feature implementations.
 */
@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow(remap = false)
    public abstract boolean isHovered(int mouseX, int mouseY);

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1), remap = false)
    private void cobbleaid$renderCustomElements(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        CobbleAid mod = CobbleAid.getInstance();
        ModConfig config = mod.getConfig();
        
        // Check if mod is globally disabled first
        if (config.modDisabled) {
            return;
        }
        
        Pokemon pokemon = getPokemon();
        if (pokemon == null) {
            return;
        }

        // Delegate to feature implementations - they handle their own enabled checks
        if (pokemon instanceof NeoDaycareDummyPokemon) {
            mod.getPcEggsFeature().renderEggPreview(context, (NeoDaycareDummyPokemon) pokemon, posX, posY);
        }
        
        mod.getPcIconsFeature().renderIcons(context, pokemon, posX, posY);
    }
}
