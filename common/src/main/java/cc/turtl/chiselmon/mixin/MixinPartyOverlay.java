package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import com.cobblemon.mod.common.client.gui.PartyOverlay;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static cc.turtl.chiselmon.client.feature.eggspy.EggPreview.eggHatchRatio;

@Mixin(PartyOverlay.class)
public class MixinPartyOverlay {

    // Force the egg hatch percentage into the xp bar if enabled
    @ModifyVariable(method = "render", at = @At(value = "LOAD"), name = "expRatio")
    private float chiselmon$modifyExpRatio(float expRatio, @Local(name = "pokemon") Pokemon pokemon) {
        return eggHatchRatio(expRatio, pokemon);
    }

    // Force the egg hatch percentage into the health bar if enabled
    @ModifyVariable(method = "render", at = @At(value = "LOAD"), name = "hpRatio")
    private float chiselmon$modifyHpRatio(float hpRatio, @Local(name = "pokemon") Pokemon pokemon) {
        return eggHatchRatio(hpRatio, pokemon);
    }

    // Redirect species/form/aspects to the hatchling preview individually
    // since PartyOverlay constructs its own render from the pokemon data
    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getSpecies()Lcom/cobblemon/mod/common/pokemon/Species;", remap = false)
    )
    private Species chiselmon$swapSpeciesForDisplay(Species original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getSpecies();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getForm()Lcom/cobblemon/mod/common/pokemon/FormData;", remap = false)
    )
    private FormData chiselmon$swapFormForDisplay(FormData original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getForm();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getAspects()Ljava/util/Set;", remap = false)
    )
    private java.util.Set<String> chiselmon$swapAspectsForDisplay(java.util.Set<String> original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getAspects();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getDisplayName$default(Lcom/cobblemon/mod/common/pokemon/Pokemon;ZILjava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", remap = false)
    )
    private MutableComponent chiselmon$swapDisplayNameForDisplay(MutableComponent original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getDisplayName(false);
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getGender()Lcom/cobblemon/mod/common/pokemon/Gender;", remap = false)
    )
    private Gender chiselmon$swapGenderForDisplay(Gender original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getGender();
    }
}
