package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import cc.turtl.cobbleaid.gui.pc.PcSortUIHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen implements PcSortUIHandler.ButtonAdder {

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

    @Inject(method = "init", at = @At("TAIL"))
    private void injectClientSideSortButton(CallbackInfo ci) {
        // Delegate all setup logic to the external handler
        PcSortUIHandler.initializeSortButtons(
                (PCGUI)(Object)this,
                this.pc,
                this.storageWidget,
                this, // Pass 'this' as the ButtonAdder
                this.width,
                this.height,
                BASE_WIDTH,
                BASE_HEIGHT);
    }
}