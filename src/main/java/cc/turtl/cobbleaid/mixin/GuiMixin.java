package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.item.interactive.PokerodItem;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.hud.PokeRodBaitOverlay;
import cc.turtl.cobbleaid.service.ConfigService;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    protected ItemStack lastToolHighlight;

    // Cancel poke rod name rendering and replace with bait info
    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void hideSelectedItemNameForPokeRod(GuiGraphics guiGraphics, CallbackInfo ci) {
        ConfigService configService = CobbleAid.services().config();
        ModConfig config = configService.get();

        if (!config.modDisabled && config.showPokeRodBaitAboveHotbar
                && lastToolHighlight.getItem() instanceof PokerodItem) {
            PokeRodBaitOverlay.renderPokeRodOverlay(guiGraphics);
            ci.cancel();
        }
    }
}
