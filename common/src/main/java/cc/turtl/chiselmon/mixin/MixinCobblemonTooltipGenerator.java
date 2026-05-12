package cc.turtl.chiselmon.mixin;

import cc.turtl.turtlshell.api.core.format.ColorLib;
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

import java.util.EnumSet;
import java.util.List;

import static cc.turtl.chiselmon.core.util.format.ComponentUtilsKt.*;
import static cc.turtl.turtlshell.api.core.format.StringFormatsKt.capitalizeFirst;

@Mixin(value = CobblemonTooltipGenerator.class)
public abstract class MixinCobblemonTooltipGenerator {

    @Inject(method = "generateTooltip", at = @At("TAIL"), remap = false)
    private void chiselmon$onGenerateTooltip(ItemStack stack, List<Component> lines, CallbackInfoReturnable<List<Component>> cir) {
        if (!(stack.getItem() instanceof BerryItem berryItem)) return;

        Berry berry = berryItem.berry();
        if (berry == null) return;

        EnumSet<MulchVariant> favoriteMulches = berry.getFavoriteMulches();
        if (favoriteMulches.isEmpty()) return;

        List<Component> tooltipLines = cir.getReturnValue();
        if (tooltipLines == null) return;

        MutableComponent mulchHint = labelled(
                Component.translatable("chiselmon.ui.label.preferred_mulch"),
                join(favoriteMulches, ", ",
                        mulch -> createComponent(
                                capitalizeFirst(mulch.getSerializedName()),
                                ColorLib.INSTANCE.getWHITE().getRGB(), false)
                ));

        tooltipLines.add(mulchHint);
    }
}
