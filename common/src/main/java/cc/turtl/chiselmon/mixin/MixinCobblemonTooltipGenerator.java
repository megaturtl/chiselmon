package cc.turtl.chiselmon.mixin;

import com.cobblemon.mod.common.api.berry.Berry;
import com.cobblemon.mod.common.api.mulch.MulchVariant;
import com.cobblemon.mod.common.client.tooltips.CobblemonTooltipGenerator;
import com.cobblemon.mod.common.item.berry.BerryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static cc.turtl.chiselmon.core.util.format.ComponentUtilsKt.join;

@Mixin(value = CobblemonTooltipGenerator.class)
public abstract class MixinCobblemonTooltipGenerator {

    @Inject(method = "generateTooltip", at = @At("TAIL"), remap = false, cancellable = true)
    private void chiselmon$onGenerateTooltip(ItemStack stack, List<Component> lines, CallbackInfoReturnable<List<Component>> cir) {
        if (stack.getItem() instanceof BerryItem berryItem) {
            Berry berry = berryItem.berry();

            if (berry != null && !berry.getFavoriteMulches().isEmpty()) {
                MutableComponent mulchHint = Component.literal("Preferred Mulch: ");
                mulchHint.append(join(berry.getFavoriteMulches(), ", ", mulch -> (Component.literal(mulch.getSerializedName()))));

                List<Component> tooltipLines = cir.getReturnValue();
                tooltipLines.add(mulchHint);
                cir.setReturnValue(tooltipLines);
            }
        }
    }
}
