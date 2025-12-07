package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.PokemonTooltip;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.gui.pc.StorageSlotTooltipState;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEggData;
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
    private void cobbleaid$renderStorageTooltips(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
        
        final Pokemon tooltipPokemon;
        if (NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
            tooltipPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();
        } else {
            tooltipPokemon = pokemon;
        }
        
        if (tooltipPokemon != null) {
            List<Component> tooltip = new ArrayList<>();

            tooltip.add(PokemonTooltip.nameTooltip(tooltipPokemon));
            if (config.showDetailedTooltipOnShift && Screen.hasShiftDown()) {
                tooltip.add(PokemonTooltip.iVsTooltip(tooltipPokemon));
                tooltip.add(Component.literal("§dOT: §f" + tooltipPokemon.getOriginalTrainerName()));
                tooltip.add(Component.literal("§dFriendship: §f" + tooltipPokemon.getFriendship()));
                tooltip.add(Component.literal("§dMarks: §f" + tooltipPokemon.getMarks().size()));
                tooltip.add(Component.literal("§dForm: §f" + tooltipPokemon.getForm().getName()));
            }

            context.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    tooltip,
                    StorageSlotTooltipState.getTooltipMouseX(),
                    StorageSlotTooltipState.getTooltipMouseY());
        }
        
        // Clear for next frame
        StorageSlotTooltipState.clear();
    }
}