package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.feature.pc.eggspy.EggDummy;
import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PartyOverlay.class)
public class MixinPartyOverlay {

    @ModifyVariable(
            method = "render",
            at = @At(value = "LOAD"),
            name = "expRatio"
    )
    private float chiselmon$modifyExpRatio(
            float expRatio,
            @Local(name = "pokemon") Pokemon pokemon) {

        if (pokemon == null) return expRatio;
        Pokemon preview = ((DuckPreviewPokemon) pokemon).chiselmon$getPreview();
        if (preview instanceof EggDummy egg) {
            return egg.getHatchCompletion();
        }
        return expRatio;
    }
}