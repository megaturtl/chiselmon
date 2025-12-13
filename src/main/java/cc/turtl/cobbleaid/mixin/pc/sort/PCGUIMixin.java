package cc.turtl.cobbleaid.mixin.pc.sort;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SortPCBoxPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.service.ConfigService;
import cc.turtl.cobbleaid.feature.pc.sort.PcSortUIHandler;
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

    private final ConfigService configService = CobbleAid.services().config();

    // Add custom sort buttons and tab buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addSortElements(CallbackInfo ci) {
        ModConfig config = configService.get();
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
        ModConfig config = configService.get();
        if (config.modDisabled || !config.pc.quickSortEnabled) {
            return;
        }

        // middle mouse click
        if (button == 2) {
            cobbleaid$executeQuickSort();
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void cobbleaid$executeQuickSort() {

        if (this.storageWidget != null) {
            this.storageWidget.resetSelected();
        }

        new SortPCBoxPacket(
                this.pc.getUuid(),
                this.storageWidget.getBox(),
                configService.get().pc.quickSortMode,
                hasShiftDown()).sendToServer();
    }
}
