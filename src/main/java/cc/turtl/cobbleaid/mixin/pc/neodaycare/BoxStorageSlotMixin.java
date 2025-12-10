package cc.turtl.cobbleaid.mixin.pc.neodaycare;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.gui.pc.BoxStorageSlot;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;

@Mixin(BoxStorageSlot.class)
public abstract class BoxStorageSlotMixin {

    @Shadow
    @NotNull
    private ClientPC pc;

    @Shadow
    @NotNull
    private PCPosition position;

    @Overwrite
    @Nullable
    public Pokemon getPokemon() {
        Pokemon pokemon = this.pc.get(this.position);
        return NeoDaycareEgg.getDummyOrOriginal(pokemon);
    }
}