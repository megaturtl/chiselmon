package cc.turtl.chiselmon.mixin.pc.tooltip;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.util.PokemonFormatUtil;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;
import cc.turtl.chiselmon.feature.pc.StorageSlotTooltipState;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.StringFormats;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

// needs to run after morecobblemontweaks multiselect mixin
@Mixin(value = StorageWidget.class, priority = 2000)
public class StorageWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void chiselmon$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta,
                                                 CallbackInfo ci) {
        if (Chiselmon.isDisabled())
            return;
        ChiselmonConfig config = Chiselmon.services().config().get();

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
                MutableComponent ivsLabel = ComponentUtil.modTranslatable("ui.label.ivs");
                tooltip.add(ComponentUtil.labelledValue(ivsLabel, PokemonFormatUtil.hypertrainedIVs(pokemon)));
            }

            if (config.pc.tooltip.showOriginalTrainer) {
                MutableComponent originalTrainerLabel = ComponentUtil.modTranslatable("ui.label.original_trainer");
                tooltip.add(ComponentUtil.labelledValue(originalTrainerLabel, pokemon.getOriginalTrainerName()));
            }

            if (config.pc.tooltip.showForm) {
                MutableComponent formLabel = ComponentUtil.modTranslatable("ui.label.form");
                tooltip.add(ComponentUtil.labelledValue(formLabel, pokemon.getForm().getName()));
            }

            if (config.pc.tooltip.showFriendship) {
                MutableComponent friendshipLabel = ComponentUtil.modTranslatable("ui.label.friendship");
                tooltip.add(ComponentUtil.labelledValue(friendshipLabel, pokemon.getFriendship()));
            }

            if (config.pc.tooltip.showRideStyles && PokemonPredicates.IS_RIDEABLE.test(pokemon)) {
                MutableComponent rideStylesLabel = ComponentUtil.modTranslatable("ui.label.ride_styles");
                tooltip.add(ComponentUtil.labelledValue(rideStylesLabel, PokemonFormatUtil.rideStyles(pokemon)));
            }

            if (config.pc.tooltip.showMarks && PokemonPredicates.IS_MARKED.test(pokemon)) {
                MutableComponent marksLabel = ComponentUtil.modTranslatable("ui.label.marks");
                tooltip.add(ComponentUtil.labelledValue(marksLabel, PokemonFormatUtil.marks(pokemon)));
            }

            if (config.pc.tooltip.showEggCycles && pokemon instanceof NeoDaycareEggDummy dummy) {
                MutableComponent eggCyclesLabel = ComponentUtil.modTranslatable("ui.label.egg_cycles");
                int cycles = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName()).eggCycles;
                tooltip.add(ComponentUtil.labelledValue(eggCyclesLabel,
                        dummy.getCyclesCompleted() + "/" + cycles + " ("
                                + StringFormats.formatPercentage(dummy.getHatchCompletion()) + ")"));
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
