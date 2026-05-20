package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import com.cobblemon.mod.common.client.gui.summary.widgets.PartySlotWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static cc.turtl.chiselmon.client.feature.eggspy.EggPreview.eggHatchRatio;

@Mixin(PartySlotWidget.class)
public abstract class MixinPartySlotWidget {

    @Final
    @Shadow
    private Pokemon pokemon;

    @ModifyVariable(method = "renderWidget", at = @At("STORE"), name = "slotPokemon")
    @Nullable
    private Pokemon chiselmon$swapPokemonForDisplay(@Nullable Pokemon slotPokemon) {
        if (slotPokemon == null) return null;
        return EggPreview.forDisplay(slotPokemon);
    }

    @ModifyVariable(method = "renderWidget", at = @At(value = "LOAD"), name = "hpRatio")
    private float chiselmon$modifyHpRatio(float hpRatio) {
        return eggHatchRatio(hpRatio, pokemon);
    }
}