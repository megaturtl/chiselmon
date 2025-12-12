package cc.turtl.cobbleaid.feature.pc.tooltips;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.format.FormatUtil;
import cc.turtl.cobbleaid.api.format.PokemonFormatUtil;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders tooltips for Pokemon in PC storage slots.
 * This class encapsulates all tooltip rendering logic.
 */
public class StorageSlotTooltipRenderer {
    
    public StorageSlotTooltipRenderer() {
    }
    
    public void render(GuiGraphics context, int mouseX, int mouseY) {
        StorageSlot hoveredSlot = StorageSlotTooltipState.getHoveredSlot();
        if (hoveredSlot == null) {
            return;
        }
        
        Pokemon pokemon = hoveredSlot.getPokemon();
        if (pokemon == null) {
            StorageSlotTooltipState.clear();
            return;
        }
        
        List<Component> tooltip = buildTooltip(pokemon);
        
        context.renderComponentTooltip(
            Minecraft.getInstance().font,
            tooltip,
            StorageSlotTooltipState.getTooltipMouseX(),
            StorageSlotTooltipState.getTooltipMouseY()
        );
        
        // Clear for next frame
        StorageSlotTooltipState.clear();
    }
    
    private List<Component> buildTooltip(Pokemon pokemon) {
        List<Component> tooltip = new ArrayList<>();
        
        tooltip.add(PokemonFormatUtil.detailedPokemonName(pokemon));
        
        if (CobbleAid.getInstance().getConfig().pc.tooltip.showDetailedTooltipOnShift 
                && Screen.hasShiftDown()) {
            tooltip.add(FormatUtil.labelledValue("IVs: ", PokemonFormatUtil.hypertrainedIVs(pokemon)));
            tooltip.add(FormatUtil.labelledValue("OT: ", pokemon.getOriginalTrainerName()));
            tooltip.add(FormatUtil.labelledValue("Marks: ", pokemon.getMarks().size()));
            tooltip.add(FormatUtil.labelledValue("Friendship: ", pokemon.getFriendship()));
            tooltip.add(FormatUtil.labelledValue("Form: ", pokemon.getForm().getName()));
        }
        
        return tooltip;
    }
}
