package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MovesWidget.class)
public abstract class MixinMovesWidget extends SoundlessWidget {
    // Dummy constructor
    public MixinMovesWidget(int pX, int pY, int pWidth, int pHeight, @NotNull Component component) {
        super(pX, pY, pWidth, pHeight, component);
    }

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/gui/summary/Summary;getSelectedPokemon$common()Lcom/cobblemon/mod/common/pokemon/Pokemon;", remap = false)
    )
    private Pokemon chiselmon$swapPokemonForDisplay(Pokemon original) {
        return EggPreview.forDisplay(original);
    }
}