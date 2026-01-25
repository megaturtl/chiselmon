package cc.turtl.chiselmon.mixin.eggpreview;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.module.feature.EggPreviewModule;

@Mixin(ClientPC.class)
public class ClientPCMixin {

    @Inject(method = "get(Lcom/cobblemon/mod/common/api/storage/pc/PCPosition;)Lcom/cobblemon/mod/common/pokemon/Pokemon;", at = @At("RETURN"), cancellable = true, remap = false)
    // Intercepts getting the pokemon and replaces with a dummy if it's an egg
    private void onGetPokemon(PCPosition position, CallbackInfoReturnable<Pokemon> cir) {
        EggPreviewModule module = Chiselmon.modules().getModule(EggPreviewModule.class);
        if (module == null) {
            return;
        }
        Pokemon updated = module.onGetPokemon(cir.getReturnValue());
        if (updated != cir.getReturnValue()) {
            cir.setReturnValue(updated);
        }
    }

    @Inject(method = "getPosition(Lcom/cobblemon/mod/common/pokemon/Pokemon;)Lcom/cobblemon/mod/common/api/storage/pc/PCPosition;", at = @At("HEAD"), cancellable = true, remap = false)
    // If a dummy is passed to this method, return the original pokemon
    // so that backend packet stuff still works
    private void onGetPosition(Pokemon pokemon, CallbackInfoReturnable<PCPosition> cir) {
        EggPreviewModule module = Chiselmon.modules().getModule(EggPreviewModule.class);
        if (module == null) {
            return;
        }
        PCPosition position = module.onGetPosition((ClientPC) (Object) this, pokemon);
        if (position != null) {
            cir.setReturnValue(position);
        }
    }
}
