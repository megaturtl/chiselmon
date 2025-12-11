package cc.turtl.cobbleaid.mixin.pc.tooltip;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.PokemonTooltips;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.tooltip.StorageSlotTooltipState;
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

@Mixin(StorageWidget.class)
public class StorageWidgetMixin {

    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void cobbleaid$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        ModConfig config = CobbleAid.getInstance().getConfig();

        if (config.modDisabled || !config.showTooltips) {
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

        tooltip.add(PokemonTooltips.nameTooltip(pokemon));
        if (config.showDetailedTooltipOnShift && Screen.hasShiftDown()) {
            tooltip.add(PokemonTooltips.iVsTooltip(pokemon));
            tooltip.add(PokemonTooltips.labeledTooltip("OT: ", pokemon.getOriginalTrainerName()));
            tooltip.add(PokemonTooltips.labeledTooltip("Marks: ", pokemon.getMarks().size()));
            tooltip.add(PokemonTooltips.labeledTooltip("Friendship: ", pokemon.getFriendship()));
            tooltip.add(PokemonTooltips.labeledTooltip("Form: ", pokemon.getForm().getName()));
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