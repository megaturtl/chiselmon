package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.duck.DuckGlowableEntity;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements DuckGlowableEntity {

    @Unique private Integer chiselmon$clientGlowColor = null;

    @Override
    public void chiselmon$setClientGlowColor(Integer rgb) {
        this.chiselmon$clientGlowColor = rgb;
    }

    @Override
    public Integer chiselmon$getClientGlowColor() {
        return chiselmon$clientGlowColor;
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (this.chiselmon$clientGlowColor != null
                && !ChiselmonConfig.INSTANCE.getGeneral().getModDisabled()) {
            cir.setReturnValue(this.chiselmon$clientGlowColor);
        }
    }
}
