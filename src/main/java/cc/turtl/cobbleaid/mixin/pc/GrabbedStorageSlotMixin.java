package cc.turtl.cobbleaid.mixin.pc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.cobblemon.mod.common.client.gui.pc.GrabbedStorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;

@Mixin(GrabbedStorageSlot.class)
public abstract class GrabbedStorageSlotMixin {

    @Shadow
    @NotNull
    private Pokemon pokemon;

    @Overwrite
    @Nullable
    public Pokemon getPokemon() {
        return NeoDaycareEgg.getEggPreviewPokemon(pokemon);
    }
}