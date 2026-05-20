package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Summary.class)
public abstract class MixinSummary extends Screen {
    @Shadow
    private Pokemon selectedPokemon;

    // Dummy constructor
    protected MixinSummary(Component title) {
        super(title);
    }

    @ModifyExpressionValue(
            method = "updatePokemonInfo",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;asRenderablePokemon()Lcom/cobblemon/mod/common/pokemon/RenderablePokemon;", remap = false)
    )
    private RenderablePokemon chiselmon$swapRenderableForDisplay(RenderablePokemon original) {
        Pokemon preview = EggPreview.forDisplay(selectedPokemon);
        return preview == selectedPokemon ? original : preview.asRenderablePokemon();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getGender()Lcom/cobblemon/mod/common/pokemon/Gender;", remap = false)
    )
    private Gender chiselmon$swapGenderForDisplay(Gender original) {
        Pokemon preview = EggPreview.forDisplay(selectedPokemon);
        return preview == selectedPokemon ? original : preview.getGender();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getPrimaryType()Lcom/cobblemon/mod/common/api/types/ElementalType;", remap = false)
    )
    private ElementalType chiselmon$swapPrimaryTypeForDisplay(ElementalType original) {
        Pokemon preview = EggPreview.forDisplay(this.selectedPokemon);
        return preview == this.selectedPokemon ? original : preview.getPrimaryType();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getSecondaryType()Lcom/cobblemon/mod/common/api/types/ElementalType;", remap = false)
    )
    private ElementalType chiselmon$swapSecondaryTypeForDisplay(ElementalType original) {
        Pokemon preview = EggPreview.forDisplay(this.selectedPokemon);
        return preview == this.selectedPokemon ? original : preview.getSecondaryType();
    }

    @ModifyArg(
            method = "displayMainScreen",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/gui/summary/widgets/screens/info/InfoWidget;<init>(IILcom/cobblemon/mod/common/pokemon/Pokemon;)V", remap = false),
            index = 2
    )
    private Pokemon chiselmon$swapInfoWidgetPokemonForDisplay(Pokemon original) {
        return EggPreview.forDisplay(original);
    }

    @ModifyArg(
            method = "displayMainScreen",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/gui/summary/widgets/screens/stats/StatWidget;<init>(IILcom/cobblemon/mod/common/pokemon/Pokemon;I)V", remap = false),
            index = 2
    )
    private Pokemon chiselmon$swapStatWidgetPokemonForDisplay(Pokemon original) {
        return EggPreview.forDisplay(original);
    }

}
