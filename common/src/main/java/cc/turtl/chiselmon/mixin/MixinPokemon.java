package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.eggspy.EggCache;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.pokemon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cc.turtl.chiselmon.feature.pc.eggspy.EggDummy.EGG_SPECIES_ID;

@Mixin(Pokemon.class)
public abstract class MixinPokemon implements DuckPreviewPokemon {

    @Shadow
    private Species species;

    @Override
    public boolean chiselmon$isEgg() {
        // This bypasses the redirected getSpecies() method to prevent recursion with the cache lol
        return EGG_SPECIES_ID.equals(this.species.getResourceIdentifier());
    }

    @Unique
    @Override
    public Pokemon chiselmon$getPreview() {
        ChiselmonConfig config = ChiselmonConfig.get();
        Pokemon self = (Pokemon) (Object) this;
        if (config.general.modDisabled || !config.pc.eggSpy.enabled) return self;
        else return EggCache.getPreview(self);
    }

    // Redirect getters used by UIs
    @Inject(method = "getSpecies", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectSpecies(CallbackInfoReturnable<Species> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getSpecies());
        }
    }

    @Inject(method = "getNickname", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectNickname(CallbackInfoReturnable<MutableComponent> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            MutableComponent name = preview.getNickname();
            cir.setReturnValue(name != null
                    ? Component.literal("(EGG) ").append(name)
                    : Component.literal("(EGG) ").append(preview.getSpecies().getName()));
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectDisplayName(CallbackInfoReturnable<MutableComponent> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            MutableComponent name = preview.getNickname();
            cir.setReturnValue(name != null
                    ? Component.literal("(EGG) ").append(name)
                    : Component.literal("(EGG) ").append(preview.getSpecies().getName()));
        }
    }

    @Inject(method = "getGender", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectGender(CallbackInfoReturnable<Gender> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getGender());
        }
    }

    @Inject(method = "getNature", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectNature(CallbackInfoReturnable<Nature> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getNature());
        }
    }

    @Inject(method = "getAbility", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectAbility(CallbackInfoReturnable<Ability> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getAbility());
        }
    }

    @Inject(method = "getIvs", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectIvs(CallbackInfoReturnable<IVs> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getIvs());
        }
    }

    @Inject(method = "getScaleModifier", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectScaleModifier(CallbackInfoReturnable<Float> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(preview.getScaleModifier());
        }
    }

    @Inject(method = "asRenderablePokemon", at = @At("HEAD"), cancellable = true)
    private void chiselmon$redirectRenderablePokemon(CallbackInfoReturnable<RenderablePokemon> cir) {
        Pokemon preview = chiselmon$getPreview();
        if (preview != (Object) this) {
            cir.setReturnValue(new RenderablePokemon(preview.getSpecies(), preview.getAspects(), ItemStack.EMPTY));
        }
    }

    @Unique
    @Override
    public RenderablePokemon chiselmon$getRawRenderablePokemon() {
        // Access species directly from shadow field, no redirect
        return new RenderablePokemon(species, ((Pokemon)(Object)this).getAspects(), ItemStack.EMPTY);
    }
}