package cc.turtl.chiselmon.mixin.accessor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.cobblemon.mod.common.api.abilities.Ability;

@Mixin(value = Pokemon.class, remap = false)
public interface AccessorPokemon {
    @Accessor("ability")
    void chiselmon$setAbility(Ability ability);

    @Accessor("isClient")
    void chiselmon$setIsClient(boolean isClient);
}