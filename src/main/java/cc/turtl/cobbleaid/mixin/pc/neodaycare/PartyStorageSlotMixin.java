package cc.turtl.cobbleaid.mixin.pc.neodaycare;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.client.gui.pc.PartyStorageSlot;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEgg;

@Mixin(PartyStorageSlot.class)
public abstract class PartyStorageSlotMixin {

    @Shadow
    @NotNull
    private ClientParty party;

    @Shadow
    @NotNull
    private PartyPosition position;

    @Overwrite
    @Nullable
    public Pokemon getPokemon() {
        Pokemon pokemon = this.party.get(this.position);
        return NeoDaycareEgg.getDummyOrOriginal(pokemon);
    }
}