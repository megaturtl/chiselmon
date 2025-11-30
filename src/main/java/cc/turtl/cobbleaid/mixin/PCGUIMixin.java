package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.neodaycare.NeoDaycareEggData;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.gui.pc.PcSortUIHandler;
import cc.turtl.cobbleaid.mixin.accessor.PCGUIAccessor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Unique
    private final PCGUIAccessor accessor = (PCGUIAccessor) (Object) this;

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

    ModConfig config = CobbleAid.getInstance().getConfig();

    // Add custom sort buttons to the options menu
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$injectClientSideSortButton(CallbackInfo ci) {
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

    // If pokemon is an egg, replace the egg with its data to render in the PC side preview
    // Uses accessors and invokers to get private methods and simulate the real setPreviewPokemon method
    @Inject(method = "setPreviewPokemon", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleaid$replaceEggWithDummy(Pokemon pokemon, boolean isParty, CallbackInfo ci) {

        if (config.showEggPreview != false && pokemon != null && NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {

            Pokemon eggDummyPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();

            PCGUI self = (PCGUI) (Object) this;

            Boolean isPreviewInParty = this.accessor.getIsPreviewInParty();

            this.accessor.invokeSaveMarkings(isPreviewInParty != null && isPreviewInParty.booleanValue());

            // Set the current previewPokemon to the dummy
            this.previewPokemon = eggDummyPokemon;

            int x = (self.width - BASE_WIDTH) / 2;
            int y = (self.height - BASE_HEIGHT) / 2;

            // Draw the model widget for the dummy
            this.accessor.setModelWidget(
                    new ModelWidget(
                            x + 6,
                            y + 27,
                            PCGUI.PORTRAIT_SIZE,
                            PCGUI.PORTRAIT_SIZE,
                            eggDummyPokemon.asRenderablePokemon(),
                            2F,
                            325F,
                            -10.0,
                            true,
                            true
                    ));

            this.accessor.getMarkingsWidget().setActivePokemon(this.previewPokemon);

            // Cancel the original method execution
            ci.cancel();
            return;
        }
    }
}