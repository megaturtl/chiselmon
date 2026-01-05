package cc.turtl.chiselmon.mixin.pc.tooltip;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.util.PokemonFormatUtil;
import cc.turtl.chiselmon.compat.neodaycare.NeoDaycareDummyPokemon;
import cc.turtl.chiselmon.config.ModConfig;
import cc.turtl.chiselmon.feature.pc.StorageSlotTooltipState;
import cc.turtl.chiselmon.util.ComponentFormatUtil;
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
    private void chiselmon$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        if (Chiselmon.isDisabled())
            return;
        ModConfig config = Chiselmon.services().config().get();

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
            if (config.pc.tooltip.showIvs) {
                tooltip.add(ComponentFormatUtil.labelledValue("IVs: ", PokemonFormatUtil.hypertrainedIVs(pokemon)));
            }

            if (config.pc.tooltip.showOriginalTrainer) {
                tooltip.add(ComponentFormatUtil.labelledValue("OT: ", pokemon.getOriginalTrainerName()));
            }

            if (config.pc.tooltip.showForm) {
                tooltip.add(ComponentFormatUtil.labelledValue("Form: ", pokemon.getForm().getName()));
            }

            if (config.pc.tooltip.showFriendship) {
                tooltip.add(ComponentFormatUtil.labelledValue("Friendship: ", pokemon.getFriendship()));
            }

            if (config.pc.tooltip.showRideStyles && PokemonPredicates.IS_RIDEABLE.test(pokemon)) {
                tooltip.add(ComponentFormatUtil.labelledValue("Ride Styles: ", PokemonFormatUtil.rideStyles(pokemon)));
            }

            if (config.pc.tooltip.showMarks && PokemonPredicates.IS_MARKED.test(pokemon)) {
                tooltip.add(ComponentFormatUtil.labelledValue("Marks: ", PokemonFormatUtil.marks(pokemon)));
            }

            if (config.pc.tooltip.showEggCycles && pokemon instanceof NeoDaycareDummyPokemon dummy) {
                int cycles = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName()).eggCycles;
                tooltip.add(ComponentFormatUtil.labelledValue("Egg Cycles: ", cycles + " (~" +
                        dummy.getStepsRemaining() + " steps rem.)"));
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
