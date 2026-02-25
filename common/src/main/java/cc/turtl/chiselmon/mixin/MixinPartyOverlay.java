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

    // Force the egg hatch percentage into xp bar
    @ModifyVariable(
            method = "render",
            at = @At(value = "LOAD"),
            name = "expRatio"
    )
    private float chiselmon$modifyExpRatio(
            float expRatio,
            @Local(name = "pokemon") Pokemon pokemon) {

        if (!(pokemon instanceof DuckPreviewPokemon duckPokemon)) return expRatio;
        Pokemon preview = duckPokemon.chiselmon$getPreview();
        if (preview instanceof EggDummy egg) {
            return (float) egg.getHatchPercentage() / 100;
        }
        return expRatio;
    }

    // Force the egg hatch percentage into health bar
    @ModifyVariable(
            method = "render",
            at = @At(value = "LOAD"),
            name = "hpRatio"
    )
    private float chiselmon$modifyHpRatio(
            float hpRatio,
            @Local(name = "pokemon") Pokemon pokemon) {

        if (!(pokemon instanceof DuckPreviewPokemon duckPokemon)) return hpRatio;
        Pokemon preview = duckPokemon.chiselmon$getPreview();
        if (preview instanceof EggDummy egg) {
            return (float) egg.getHatchPercentage() / 100;
        }
        return hpRatio;
    }
}