package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.feature.eggspy.EggCache;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Need to invalidate EggCache when moving pokemon between pc/party or EggDummy fields get set to blank and then cached
 * I'm sure I made a dumb error somewhere else that caused this to begin with but oh well
 */
@Mixin(value = ClientPC.class, remap = false)
public abstract class MixinClientPC {

    // This gets called by swap, move, and set
    @Inject(
            method = "set(Lcom/cobblemon/mod/common/api/storage/pc/PCPosition;Lcom/cobblemon/mod/common/pokemon/Pokemon;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void chiselmon$onSetPokemon(PCPosition position, Pokemon pokemon, CallbackInfo ci) {
        if (pokemon != null) {
            EggCache.invalidate(pokemon.getUuid());
        }
    }
}