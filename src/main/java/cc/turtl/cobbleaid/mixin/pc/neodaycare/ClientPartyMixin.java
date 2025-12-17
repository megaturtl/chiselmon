package cc.turtl.cobbleaid.mixin.pc.neodaycare;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;

@Mixin(ClientParty.class)
public class ClientPartyMixin {

    @Inject(method = "get(Lcom/cobblemon/mod/common/api/storage/party/PartyPosition;)Lcom/cobblemon/mod/common/pokemon/Pokemon;", at = @At("RETURN"), cancellable = true, remap = false)
    // Intercepts getting the pokemon and replaces with a dummy if it's an egg
    private void onGetPokemon(PartyPosition position, CallbackInfoReturnable<Pokemon> cir) {
        Pokemon pokemon = cir.getReturnValue();
        if (pokemon != null) {
            Pokemon processed = NeoDaycareEgg.getDummyOrOriginal(pokemon);
            cir.setReturnValue(processed);
        }
    }
}