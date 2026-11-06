package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.duck.DuckGlowableEntity;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    // Entity.isCurrentlyGlowing also synchronizes vanilla flags; keep our highlight rendering-only.
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean chiselmon$addClientGlow(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || (!ChiselmonConfig.INSTANCE.getGeneral().getModDisabled()
                && entity instanceof DuckGlowableEntity glowable
                && glowable.chiselmon$getClientGlowColor() != null);
    }
}
