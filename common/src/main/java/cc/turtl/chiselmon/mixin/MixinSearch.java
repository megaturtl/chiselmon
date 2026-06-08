package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import com.cobblemon.mod.common.api.storage.pc.search.Search;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Search.class)
public class MixinSearch {

    @ModifyVariable(method = "passes", at = @At("HEAD"), argsOnly = true)
    @Nullable
    private Pokemon replacePokemon(@Nullable Pokemon pokemon) {
        if (pokemon == null) return null;
        return EggPreview.forDisplay(pokemon);
    }
}
