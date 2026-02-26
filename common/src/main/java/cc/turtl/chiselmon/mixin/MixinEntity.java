package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckGlowableEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements DuckGlowableEntity {

    @Unique
    private Integer chiselmon$clientGlowColor = null;

    @Shadow
    protected abstract void setSharedFlag(int flag, boolean value);

    @Override
    public void chiselmon$setClientGlowColor(Integer rgb) {
        this.chiselmon$clientGlowColor = rgb;
    }

    @Override
    public void chiselmon$setClientGlowing(boolean glowing) {
        this.setSharedFlag(6, glowing);
    }

    @Override
    public Integer chiselmon$getClientGlowColor() {
        return this.chiselmon$clientGlowColor;
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        if (this.chiselmon$clientGlowColor != null) {
            cir.setReturnValue(this.chiselmon$clientGlowColor);
        }
    }
}
