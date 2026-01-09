package cc.turtl.chiselmon.mixin.eggpreview;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggCache;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;

@Mixin(ClientParty.class)
public class ClientPartyMixin {

    @Inject(method = "get(Lcom/cobblemon/mod/common/api/storage/party/PartyPosition;)Lcom/cobblemon/mod/common/pokemon/Pokemon;", at = @At("RETURN"), cancellable = true, remap = false)
    // Intercepts getting the pokemon and replaces with a dummy if it's an egg
    private void onGetPokemon(PartyPosition position, CallbackInfoReturnable<Pokemon> cir) {
        if (Chiselmon.isDisabled()) return;
        Pokemon pokemon = cir.getReturnValue();
        // Only create dummy if we're not already returning a dummy
        if (NeoDaycareEggDummy.isEgg(pokemon)) {
            Pokemon processed = NeoDaycareEggCache.getDummyOrOriginal(pokemon);
            cir.setReturnValue(processed);
        }
    }
}