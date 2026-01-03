package cc.turtl.cobbleaid.mixin.pc.tooltip;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import cc.turtl.cobbleaid.api.util.PokemonFormatUtil;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.StorageSlotTooltipState;
import cc.turtl.cobbleaid.util.ComponentFormatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// needs to run after morecobblemontweaks multiselect mixin
@Mixin(value = StorageWidget.class, priority = 2000)
public class StorageWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void cobbleaid$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        if (CobbleAid.isDisabled())
            return;
        ModConfig config = CobbleAid.services().config().get();

        boolean isShiftHoverActive = config.pc.tooltip.showDetailedTooltipOnShift && Screen.hasShiftDown();

        if (!config.pc.tooltip.showTooltips && !isShiftHoverActive) {
            StorageSlotTooltipState.clear();
            return;
        }

        StorageSlot hoveredSlot = StorageSlotTooltipState.getHoveredSlot();
        if (hoveredSlot == null) {
            return;
        }

        Pokemon pokemon = hoveredSlot.getPokemon();
        if (pokemon == null) {
            StorageSlotTooltipState.clear();
            return;
        }

        List<Component> tooltip = new ArrayList<>();

        tooltip.add(PokemonFormatUtil.detailedPokemonName(pokemon));

        if (isShiftHoverActive) {
            tooltip.add(ComponentFormatUtil.labelledValue("IVs: ", PokemonFormatUtil.hypertrainedIVs(pokemon)));
            tooltip.add(ComponentFormatUtil.labelledValue("OT: ", pokemon.getOriginalTrainerName()));
            tooltip.add(ComponentFormatUtil.labelledValue("Friendship: ", pokemon.getFriendship()));
            tooltip.add(ComponentFormatUtil.labelledValue("Form: ", pokemon.getForm().getName()));

            if (PokemonPredicates.IS_RIDEABLE.test(pokemon)) {
                tooltip.add(ComponentFormatUtil.labelledValue("Ride Styles: ", PokemonFormatUtil.rideStyles(pokemon)));
            }

            if (PokemonPredicates.IS_MARKED.test(pokemon)) {
                tooltip.add(ComponentFormatUtil.labelledValue("Marks: ", PokemonFormatUtil.marks(pokemon)));
            }

        }

        context.pose().pushPose();
        // Need to shift the pose stack forward to get in front of pasture overlay
        context.pose().translate(0, 0, 200f);

        context.renderComponentTooltip(
                Minecraft.getInstance().font,
                tooltip,
                StorageSlotTooltipState.getTooltipMouseX(),
                StorageSlotTooltipState.getTooltipMouseY());

        context.pose().popPose();

        // Clear for next frame
        StorageSlotTooltipState.clear();
    }
}
