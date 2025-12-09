package cc.turtl.cobbleaid.mixin;

import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.net.messages.server.storage.pc.SortPCBoxPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import cc.turtl.cobbleaid.feature.gui.pc.PcSortUIHandler;
import cc.turtl.cobbleaid.feature.gui.pc.tab.PCBookmarkButton;
import cc.turtl.cobbleaid.feature.gui.pc.tab.PCTab;
import cc.turtl.cobbleaid.feature.gui.pc.tab.PCTabButton;
import cc.turtl.cobbleaid.feature.gui.pc.tab.PCTabManager;
import cc.turtl.cobbleaid.feature.gui.pc.tab.PCTabStore;
import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareEggData;
import cc.turtl.cobbleaid.mixin.accessor.PCGUIAccessor;
import net.minecraft.client.gui.components.Button;
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

import java.util.ArrayList;
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

    @Unique
    private final List<PCTabButton> cobbleaid$tabButtons = new ArrayList<>();

    ModConfig config = CobbleAid.getInstance().getConfig();

    // Add custom sort buttons and tab buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addCustomElements(CallbackInfo ci) {
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

        cobbleaid$rebuildTabButtons();

        Button.OnPress bookmarkToggle = (button) -> {
            PCTabStore tabStore = config.tabStore;
            int currentBoxNumber = storageWidget.getBox();
            if (tabStore.hasBoxNumber(currentBoxNumber)) {
                tabStore.removeTab(currentBoxNumber);
            } else if (tabStore.isFull()) {
                return;
            } else {
                tabStore.addTab(currentBoxNumber);
            }
            cobbleaid$rebuildTabButtons();
        };

        int guiLeft = (this.width - BASE_WIDTH) / 2;
        int guiTop = (this.height - BASE_HEIGHT) / 2;

        int bookmarkX = guiLeft + 239;
        int bookmarkY = guiTop + 13;
        PCBookmarkButton bookmarkButton = new PCBookmarkButton(bookmarkX, bookmarkY, bookmarkToggle);
        this.addRenderableWidget(bookmarkButton);
    }

    // Intercept key presses to handle quick sort keybind
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cobbleaid$handleQuickSortMouseClick(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        if (config.modDisabled || !config.quickSortEnabled) {
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
        PokemonSortMode sortMode = PokemonSortMode.POKEDEX_NUMBER;

        if (this.storageWidget != null) {
            this.storageWidget.resetSelected();
        }

        new SortPCBoxPacket(
                this.pc.getUuid(),
                this.storageWidget.getBox(),
                sortMode,
                hasShiftDown()).sendToServer();
    }

    // If pokemon is an egg, replace the egg with its data to render in the PC side
    // preview
    // Uses accessors and invokers to get private methods and simulate the real
    // setPreviewPokemon method
    @Inject(method = "setPreviewPokemon", at = @At("HEAD"), cancellable = true, remap = false)
    private void cobbleaid$replaceEggWithDummy(Pokemon pokemon, boolean isParty, CallbackInfo ci) {

        if (config.showEggPreview != false && pokemon != null && NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {

            Pokemon eggDummyPokemon = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createDummyPokemon();

            Boolean isPreviewInParty = this.accessor.getIsPreviewInParty();

            this.accessor.invokeSaveMarkings(isPreviewInParty != null && isPreviewInParty.booleanValue());

            // Set the current previewPokemon to the dummy
            this.previewPokemon = eggDummyPokemon;

            int guiLeft = (this.width - BASE_WIDTH) / 2;
            int guiTop = (this.height - BASE_HEIGHT) / 2;

            // Draw the model widget for the dummy
            this.accessor.setModelWidget(
                    new ModelWidget(
                            guiLeft + 6,
                            guiTop + 27,
                            PCGUI.PORTRAIT_SIZE,
                            PCGUI.PORTRAIT_SIZE,
                            eggDummyPokemon.asRenderablePokemon(),
                            2F,
                            325F,
                            -10.0,
                            true,
                            true));

            this.accessor.getMarkingsWidget().setActivePokemon(this.previewPokemon);

            // Cancel the original method execution
            ci.cancel();
            return;
        }
    }

    @Unique
    private void cobbleaid$rebuildTabButtons() {
        // 1. Clear existing buttons from the list and the screen
        for (PCTabButton button : this.cobbleaid$tabButtons) {
            this.removeWidget(button); // Use Screen.removeWidget to remove it from rendering
        }
        this.cobbleaid$tabButtons.clear();

        // 2. Re-calculate positions and create new buttons
        PCTabStore tabStore = config.tabStore;
        List<PCTab> tabs = tabStore.getTabs();

        int guiLeft = (this.width - BASE_WIDTH) / 2;
        int guiTop = (this.height - BASE_HEIGHT) / 2;

        int tabStartX = guiLeft + 80;
        int tabStartY = guiTop - 5;

        List<PCTabButton> newTabButtons = PCTabManager.createTabButtons(
                this.storageWidget,
                tabs,
                tabStartX,
                tabStartY);

        // 3. Add all new buttons to the screen and our tracking list
        if (!newTabButtons.isEmpty()) {
            for (PCTabButton button : newTabButtons) {
                this.addRenderableWidget(button);
                this.cobbleaid$tabButtons.add(button); // Track the new buttons
            }
        }
    }
}