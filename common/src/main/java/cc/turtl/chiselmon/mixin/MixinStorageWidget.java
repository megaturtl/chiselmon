package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.feature.pc.eggspy.EggCache;
import com.cobblemon.mod.common.client.gui.pc.BoxStorageSlot;
import com.cobblemon.mod.common.client.gui.pc.PartyStorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageWidget.class)
public class MixinStorageWidget {

    // Need to invalidate EggCache when moving pokemon to fix eggpreview staleness
    @Inject(method = "onStorageSlotClicked", at = @At("HEAD"))
    private void chiselmon$invalidateOnSlotClicked(Button button, CallbackInfo ci) {
        // Invalidate the pokemon currently in the slot (handles shift click and normal grab)
        Pokemon inSlot = null;
        if (button instanceof BoxStorageSlot slot) {
            inSlot = slot.getPokemon();
        } else if (button instanceof PartyStorageSlot slot) {
            inSlot = slot.getPokemon();
        }

        if (inSlot != null) {
            EggCache.invalidate(inSlot.getUuid());
        }

        // Invalidate the pokemon currently "on the cursor" (handles the drop/swap)
        StorageWidget self = (StorageWidget) (Object) this;
        if (self.getGrabbedSlot() != null) {
            Pokemon grabbed = self.getGrabbedSlot().getPokemon();
            if (grabbed != null) {
                EggCache.invalidate(grabbed.getUuid());
            }
        }
    }
}