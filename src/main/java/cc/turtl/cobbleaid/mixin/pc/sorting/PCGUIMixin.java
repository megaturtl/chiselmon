package cc.turtl.cobbleaid.mixin.pc.sorting;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SortPCBoxPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.pc.sorting.PcSortUIHandler;
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

    final CobbleAid INSTANCE = CobbleAid.getInstance();
    ModConfig config = INSTANCE.getConfig();

    // Add custom sort buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addSortElements(CallbackInfo ci) {
        if (config.modDisabled) {
            return;
        }
        
        // Delegate to feature
        INSTANCE.getPcSortingFeature().initializeSortButtons(
            (PCGUI) (Object) this,
            this.pc,
            this.storageWidget,
            this,
            this.width,
            this.height,
            BASE_WIDTH,
            BASE_HEIGHT
        );
    }

    // Intercept mouse clicks for quick sort
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cobbleaid$handleQuickSortMouseClick(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        if (config.modDisabled) {
            return;
        }
        
        // Delegate to feature
        if (INSTANCE.getPcSortingFeature().handleQuickSortClick(button, this.storageWidget, this.pc)) {
            cir.setReturnValue(true);
        }
    }
}