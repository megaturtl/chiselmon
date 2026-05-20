package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.config.category.GeneralConfig;
import cc.turtl.chiselmon.client.feature.eggspy.EggCache;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static cc.turtl.chiselmon.client.feature.eggspy.EggDummy.EGG_SPECIES_ID;

/**
 * Adds the {@link DuckPreviewPokemon} interface to every Pokemon. The egg preview
 * is read only by Mixins at display sites via {@link DuckPreviewPokemon#chiselmon$getPreview()};
 */
@Mixin(Pokemon.class)
public abstract class MixinPokemon implements DuckPreviewPokemon {

    @Shadow(remap = false)
    private Species species;

    // Accessed directly to avoid going through getSpecies() in case future code wraps it
    @Override
    public boolean chiselmon$isEgg() {
        return EGG_SPECIES_ID.equals(this.species.getResourceIdentifier());
    }

    @Unique
    @Override
    public Pokemon chiselmon$getPreview() {
        GeneralConfig config = ChiselmonConfig.INSTANCE.getGeneral();
        Pokemon self = (Pokemon) (Object) this;
        if (config.getModDisabled() || !config.getEggSpy().getEnabled()) return self;
        return EggCache.getPreview(self);
    }

    // Bypasses preview logic to get the egg's own renderable pokemon
    @Unique
    @Override
    public @NotNull RenderablePokemon chiselmon$getRawRenderablePokemon() {
        return new RenderablePokemon(species, ((Pokemon) (Object) this).getAspects(), ItemStack.EMPTY);
    }
}