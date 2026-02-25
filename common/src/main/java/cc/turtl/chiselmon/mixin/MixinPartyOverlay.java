package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.eggspy.EggDummy;
import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PartyOverlay.class)
public class MixinPartyOverlay {

    // Force the egg hatch percentage into the xp bar
    @ModifyVariable(method = "render", at = @At(value = "LOAD"), name = "expRatio")
    private float chiselmon$modifyExpRatio(float expRatio, @Local(name = "pokemon") Pokemon pokemon) {
        return chiselmon$setEggHatchRatio(expRatio, pokemon);
    }

    // Force the egg hatch percentage into the health bar
    @ModifyVariable(method = "render", at = @At(value = "LOAD"), name = "hpRatio")
    private float chiselmon$modifyHpRatio(float hpRatio, @Local(name = "pokemon") Pokemon pokemon) {
        return chiselmon$setEggHatchRatio(hpRatio, pokemon);
    }

    // Checks if a pokemon should have its egg hatch ratio injected into their party overlay bars and sets if true
    @Unique
    private float chiselmon$setEggHatchRatio(float fallback, Pokemon pokemon) {
        ChiselmonConfig config = ChiselmonConfig.get();

        if (config.general.modDisabled
                || !config.general.eggSpy.enabled
                || !config.general.eggSpy.showHatchOverlay
                || !(pokemon instanceof DuckPreviewPokemon duckPokemon)) {
            return fallback;
        }

        Pokemon preview = duckPokemon.chiselmon$getPreview();
        if (preview instanceof EggDummy egg) {
            return (float) egg.getHatchPercentage() / 100;
        }

        return fallback;
    }
}