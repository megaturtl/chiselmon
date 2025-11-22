package cc.turtl.cobbleaid.mixin;

import cc.turtl.cobbleaid.features.pc.PcSort;

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    @Shadow @Final public ClientPC pc;

    @Shadow(remap = false)
    private StorageWidget storageWidget;

    @Shadow(remap = false)
    @Final
    public static int BASE_WIDTH;
    @Shadow(remap = false)
    @Final
    public static int BASE_HEIGHT;

    @Shadow
    private boolean displayOptions;
    @Shadow
    private List<IconButton> optionButtons;

    protected PCGUIMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injectClientSideSortButton(CallbackInfo ci) {
        final ClientPC pc = this.pc;

        int x = (width - BASE_WIDTH) / 2;
        int y = (height - BASE_HEIGHT) / 2;

        int existingCount = PokemonSortMode.values().length;
        int btnX = x + 92 + (12 * existingCount);
        int btnY = y + 31;
        int btnWidth = 20;
        int btnHeight = 20;

        for (PcSort.SortType sortType : PcSort.SortType.values()) {
            String typeName = sortType.toString().toLowerCase();
            String tooltipKey = "ui.sort." + typeName;
            String labelKey = "sort_" + typeName;

            ResourceLocation btnTexture = ResourceLocation.fromNamespaceAndPath(
                    "cobbleaid",
                    "textures/gui/pc/pc_button_sort_" + typeName + ".png");
            ResourceLocation btnTextureAlt = ResourceLocation.fromNamespaceAndPath(
                    "cobbleaid",
                    "textures/gui/pc/pc_button_sort_" + typeName + "_reverse.png");

            IconButton customSortBtn = new IconButton(
                    btnX, btnY,
                    btnWidth, btnHeight,
                    btnTexture,
                    btnTextureAlt,
                    tooltipKey,
                    labelKey,
                    btn -> PcSort.sortPCBox(pc, this.storageWidget.getBox(), sortType, hasShiftDown()));

            // Sync with the options button
            customSortBtn.visible = this.displayOptions;

            // Add to screen and internal list
            this.addRenderableWidget(customSortBtn);
            this.optionButtons.add(customSortBtn);

            btnX += 12;
        }
    }
}