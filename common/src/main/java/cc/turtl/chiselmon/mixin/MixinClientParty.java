package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.feature.eggspy.EggCache;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Need to invalidate EggCache when moving pokemon between pc/party or EggDummy fields get set to blank and then cached
 * I'm sure I made a dumb error somewhere else that caused this to begin with but oh well
 */
@Mixin(value = ClientParty.class, remap = false)
public abstract class MixinClientParty {

    // This gets called by swap, move, and set
    @Inject(
            method = "set(Lcom/cobblemon/mod/common/api/storage/party/PartyPosition;Lcom/cobblemon/mod/common/pokemon/Pokemon;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void chiselmon$onSetPokemon(PartyPosition position, Pokemon pokemon, CallbackInfo ci) {
        if (pokemon != null) {
            EggCache.invalidate(pokemon.getUuid());
        }
    }
}