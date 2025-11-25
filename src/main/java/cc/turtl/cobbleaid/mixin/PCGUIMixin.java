package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget; // Needed for the accessor
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.neodaycare.NeoDaycareEggData;
import cc.turtl.cobbleaid.gui.pc.PcSortUIHandler;
import cc.turtl.cobbleaid.mixin.accessor.PCGUIAccessor; // Import the accessor

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

    // --- Shadowed Fields ---

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

    // NOTE: This shadow field is crucial for the replacement logic!
    @Shadow(remap = false)
    public Pokemon previewPokemon;

    // --- Unique Accessor Field ---

    // We cast 'this' to the accessor interface for private method calls
    @Unique
    private final PCGUIAccessor accessor = (PCGUIAccessor) (Object) this;

    // --- Constructor & Interface Methods (Unchanged) ---

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
                (PCGUI) (Object) this,
                this.pc,
                this.storageWidget,
                this,
                this.width,
                this.height,
                BASE_WIDTH,
                BASE_HEIGHT);
    }

    // --- Corrected Egg Replacement Logic ---

    @Inject(method = "setPreviewPokemon", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleaid$replaceEggWithDisplayModel(Pokemon pokemon, boolean isParty, CallbackInfo ci) {
        if (pokemon != null && NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {

            Pokemon displayModel = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createPokemonRepresentation();

            PCGUI self = (PCGUI) (Object) this; // Just for accessing width/height

            // 1. Call the private method using the accessor
            Boolean isPreviewInParty = this.accessor.getIsPreviewInParty();
            this.accessor.invokeSaveMarkings(isPreviewInParty != null && isPreviewInParty.booleanValue());

            // 2. Set the state using the modified model
            this.previewPokemon = displayModel;

            int x = (self.width - BASE_WIDTH) / 2;
            int y = (self.height - BASE_HEIGHT) / 2;

            // 3. Replicate ModelWidget creation using the Invoker
            this.accessor.setModelWidget(
                    new ModelWidget(
                            x + 6,
                            y + 27,
                            PCGUI.PORTRAIT_SIZE,
                            PCGUI.PORTRAIT_SIZE,
                            displayModel.asRenderablePokemon(),
                            2F,
                            325F,
                            -10.0,
                            false, // playCryOnClick: Set to true if you want the cry to play on click
                            false // shouldFollowCursor: Set to true if you want the model to look at the cursor
                    ));

            // 4. Update Markings Widget using the Invoker
            this.accessor.getMarkingsWidget().setActivePokemon(this.previewPokemon);

            // Cancel the original method execution
            ci.cancel();
            return;
        }

        // If it's null or not an egg, let the original method execute normally.
    }
}