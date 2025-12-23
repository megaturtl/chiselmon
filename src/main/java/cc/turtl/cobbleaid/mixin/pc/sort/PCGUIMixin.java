package cc.turtl.cobbleaid.mixin.pc.sort;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.ModConfig;
import cc.turtl.cobbleaid.feature.pc.sort.PcSortUIHandler;
import cc.turtl.cobbleaid.feature.pc.sort.PcSorter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen implements PcSortUIHandler.ButtonAdder {

    @Shadow
    @Final
    public ClientPC pc;
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

    @Shadow(remap = false)
    public Pokemon previewPokemon;

    protected PCGUIMixin(Component title) {
        super(title);
    }

    @Override
    public void addRenderableWidget(IconButton button) {
        super.addRenderableWidget(button);
    }

    @Override
    public boolean isDisplayingOptions() {
        return this.displayOptions;
    }

    @Override
    public List<IconButton> getOptionButtons() {
        return this.optionButtons;
    }

    // Add custom sort buttons and tab buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addSortElements(CallbackInfo ci) {
        ModConfig config = CobbleAid.services().config().get();
        if (config.modDisabled) {
            return;
        }
        PcSortUIHandler.initializeSortButtons(
                (PCGUI) (Object) this,
                this.pc,
                this.storageWidget,
                this,
                this.width,
                this.height,
                BASE_WIDTH,
                BASE_HEIGHT);
    }

    // Intercept key presses to handle quick sort keybind
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cobbleaid$handleQuickSortMouseClick(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        ModConfig config = CobbleAid.services().config().get();
        if (config.modDisabled || !config.pc.quickSortEnabled) {
            return;
        }

        // middle mouse click
        if (button == 2) {
            cobbleaid$executeQuickSort(config);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void cobbleaid$executeQuickSort(ModConfig config) {

        if (this.storageWidget != null) {
            this.storageWidget.resetSelected();
        }

        PcSorter.sortPCBox(this.pc, this.storageWidget.getBox(), config.pc.quickSortMode, hasShiftDown());
    }
}
