package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.eggspy.EggCache;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.pokemon.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cc.turtl.chiselmon.feature.pc.eggspy.EggDummy.EGG_SPECIES_ID;

@Mixin(Pokemon.class)
public abstract class MixinPokemon implements DuckPreviewPokemon {

    @Shadow(remap = false)
    private Species species;

    // Accessed directly to avoid triggering our own getSpecies() redirect
    @Override
    public boolean chiselmon$isEgg() {
        return EGG_SPECIES_ID.equals(this.species.getResourceIdentifier());
    }

    // If mid-redirect, return self to avoid infinite loops when the preview's own getters are called
    @Unique
    private boolean chiselmon$redirecting = false;

    // Cached during a redirect so inject bodies don't need to call getPreview() again
    @Unique
    private Pokemon chiselmon$pendingPreview = null;

    @Unique
    @Override
    public Pokemon chiselmon$getPreview() {
        if (chiselmon$redirecting) return (Pokemon) (Object) this;
        ChiselmonConfig config = ChiselmonConfig.get();
        Pokemon self = (Pokemon) (Object) this;
        if (config.general.modDisabled || !config.pc.eggSpy.enabled) return self;
        else return EggCache.getPreview(self);
    }

    // Returns true if no redirect is needed (not an egg, or already redirecting).
    // If a redirect is needed, caches the preview and sets the redirecting guard.
    @Unique
    private boolean chiselmon$startRedirect() {
        if (chiselmon$redirecting) return true;
        Pokemon preview = chiselmon$getPreview();
        if (preview == (Object) this) return true;
        chiselmon$pendingPreview = preview;
        chiselmon$redirecting = true;
        return false;
    }

    @Unique
    private void chiselmon$endRedirect() {
        chiselmon$redirecting = false;
        chiselmon$pendingPreview = null;
    }

    @Inject(method = "getSpecies", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectSpecies(CallbackInfoReturnable<Species> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getSpecies());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getNickname", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectNickname(CallbackInfoReturnable<MutableComponent> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            MutableComponent name = chiselmon$pendingPreview.getNickname();
            cir.setReturnValue(name != null
                    ? Component.literal("(EGG) ").append(name)
                    : Component.literal("(EGG) ").append(chiselmon$pendingPreview.getSpecies().getName()));
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectDisplayName(CallbackInfoReturnable<MutableComponent> cir) {
        chiselmon$redirectNickname(cir);
    }

    @Inject(method = "getGender", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectGender(CallbackInfoReturnable<Gender> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getGender());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getNature", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectNature(CallbackInfoReturnable<Nature> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getNature());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getAbility", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectAbility(CallbackInfoReturnable<Ability> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getAbility());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getIvs", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectIvs(CallbackInfoReturnable<IVs> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getIvs());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getScaleModifier", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectScaleModifier(CallbackInfoReturnable<Float> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getScaleModifier());
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "asRenderablePokemon", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectRenderablePokemon(CallbackInfoReturnable<RenderablePokemon> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(new RenderablePokemon(
                    chiselmon$pendingPreview.getSpecies(),
                    chiselmon$pendingPreview.getAspects(),
                    ItemStack.EMPTY));
        } finally {
            chiselmon$endRedirect();
        }
    }

    @Inject(method = "getMoveSet", at = @At("HEAD"), cancellable = true, remap = false)
    private void chiselmon$redirectMoveSet(CallbackInfoReturnable<MoveSet> cir) {
        if (chiselmon$startRedirect()) return;
        try {
            cir.setReturnValue(chiselmon$pendingPreview.getMoveSet());
        } finally {
            chiselmon$endRedirect();
        }
    }

    // Bypasses all redirect logic to get the egg's own renderable pokemon
    @Unique
    @Override
    public RenderablePokemon chiselmon$getRawRenderablePokemon() {
        return new RenderablePokemon(species, ((Pokemon) (Object) this).getAspects(), ItemStack.EMPTY);
    }
}