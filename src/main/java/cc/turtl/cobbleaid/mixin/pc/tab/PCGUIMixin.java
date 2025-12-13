package cc.turtl.cobbleaid.mixin.pc.tab;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.ModConfig;
import cc.turtl.cobbleaid.feature.pc.tab.PCBookmarkButton;
import cc.turtl.cobbleaid.feature.pc.tab.PCTab;
import cc.turtl.cobbleaid.feature.pc.tab.PCTabButton;
import cc.turtl.cobbleaid.feature.pc.tab.PCTabManager;
import cc.turtl.cobbleaid.feature.pc.tab.PCTabStore;
import cc.turtl.cobbleaid.service.ConfigService;
import cc.turtl.cobbleaid.service.ICobbleAidServices;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    protected PCGUIMixin(Component title) {
        super(title);
    }

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

    @Unique
    private PCBookmarkButton cobbleaid$bookmarkButton;

    @Unique
    private final List<PCTabButton> cobbleaid$tabButtons = new ArrayList<>();

    private final ICobbleAidServices services = CobbleAid.services();
    private final ConfigService configService = services.config();

    @Unique
    private PCTabStore cobbleaid$getTabStore() {
        return services.worldData().current().getPcTabStore();
    }

    // Add custom sort buttons and tab buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void cobbleaid$addTabElements(CallbackInfo ci) {
        ModConfig config = configService.get();
        if (config.modDisabled || !config.pc.bookmarksEnabled) {
            return;
        }

        cobbleaid$rebuildTabButtons();

        Button.OnPress bookmarkToggle = (button) -> {
            PCTabStore tabStore = cobbleaid$getTabStore();
            int currentBoxNumber = storageWidget.getBox();
            if (tabStore.hasBoxNumber(currentBoxNumber)) {
                tabStore.removeTab(currentBoxNumber);
                configService.save();
            } else if (tabStore.isFull()) {
                return;
            } else {
                tabStore.addTab(currentBoxNumber);
                configService.save();
            }
            cobbleaid$rebuildTabButtons();
        };

        int guiLeft = (this.width - BASE_WIDTH) / 2;
        int guiTop = (this.height - BASE_HEIGHT) / 2;

        int bookmarkX = guiLeft + 239;
        int bookmarkY = guiTop + 13;
        PCBookmarkButton bookmarkButton = new PCBookmarkButton(bookmarkX, bookmarkY, bookmarkToggle);
        this.cobbleaid$bookmarkButton = bookmarkButton;
        this.addRenderableWidget(bookmarkButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cobbleaid$updateBookmarkButtonState(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        ModConfig config = configService.get();
        if (config.modDisabled || !config.pc.bookmarksEnabled || this.cobbleaid$bookmarkButton == null) {
            return;
        }
        PCTabStore tabStore = cobbleaid$getTabStore();

        boolean isCurrentBoxBookmarked = tabStore.hasBoxNumber(storageWidget.getBox());
        this.cobbleaid$bookmarkButton.setToggled(isCurrentBoxBookmarked);
    }

    @Unique
    private void cobbleaid$rebuildTabButtons() {
        // 1. Clear existing buttons from the list and the screen
        for (PCTabButton button : this.cobbleaid$tabButtons) {
            this.removeWidget(button); // Use Screen.removeWidget to remove it from rendering
        }
        this.cobbleaid$tabButtons.clear();

        // 2. Re-calculate positions and create new buttons
        PCTabStore tabStore = cobbleaid$getTabStore();
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
