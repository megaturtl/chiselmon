package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.features.pc.PcHighlight;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {

    @Shadow public abstract Pokemon getPokemon();

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void cobbleaid$renderFilterBackground(GuiGraphics context, int x, int y, float delta, CallbackInfo ci) {
        Pokemon pokemon = getPokemon();
        if (pokemon == null) {
            return;
        }

        Integer color = PcHighlight.getHighlightColor(pokemon);
        if (color == null) {
            return;
        }

        context.fill(x, y, x + StorageSlot.SIZE, y + StorageSlot.SIZE, color);
    }
}

