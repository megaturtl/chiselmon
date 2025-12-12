package cc.turtl.cobbleaid.mixin.pc.tooltips;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.format.FormatUtil;
import cc.turtl.cobbleaid.api.format.PokemonFormatUtil;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.tooltips.StorageSlotTooltipState;
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
        CobbleAid mod = CobbleAid.getInstance();
        ModConfig config = mod.getConfig();

        if (config.modDisabled) {
            StorageSlotTooltipState.clear();
            return;
        }
        
        // Use feature system to check if tooltips are enabled
        if (!mod.getPcTooltipsFeature().isEnabled()) {
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
        if (config.pc.tooltip.showDetailedTooltipOnShift && Screen.hasShiftDown()) {
            tooltip.add(FormatUtil.labelledValue("IVs: ", PokemonFormatUtil.hypertrainedIVs(pokemon)));
            tooltip.add(FormatUtil.labelledValue("OT: ", pokemon.getOriginalTrainerName()));
            tooltip.add(FormatUtil.labelledValue("Marks: ", pokemon.getMarks().size()));
            tooltip.add(FormatUtil.labelledValue("Friendship: ", pokemon.getFriendship()));
            tooltip.add(FormatUtil.labelledValue("Form: ", pokemon.getForm().getName()));
        }

        context.renderComponentTooltip(
                Minecraft.getInstance().font,
                tooltip,
                StorageSlotTooltipState.getTooltipMouseX(),
                StorageSlotTooltipState.getTooltipMouseY());

        // Clear for next frame
        StorageSlotTooltipState.clear();
    }
}