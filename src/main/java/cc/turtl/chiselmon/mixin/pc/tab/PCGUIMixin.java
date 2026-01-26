package cc.turtl.chiselmon.mixin.pc.tab;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.tab.*;
import cc.turtl.chiselmon.service.ConfigService;
import cc.turtl.chiselmon.service.IChiselmonServices;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
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
    private PCBookmarkButton chiselmon$bookmarkButton;

    @Unique
    private PCHomeButton chiselmon$homeButton;

    @Unique
    private final List<PCTabButton> chiselmon$tabButtons = new ArrayList<>();

    private final IChiselmonServices services = Chiselmon.services();
    private final ConfigService configService = services.config();

    @Unique
    private PCTabStore chiselmon$getTabStore() {
        return services.worldData().current().getPcTabStore();
    }

    // Add custom sort buttons and tab buttons
    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$addTabElements(CallbackInfo ci) {
        if (Chiselmon.isDisabled())
            return;
        ChiselmonConfig config = configService.get();
        if (!config.pc.bookmarksEnabled) {
            return;
        }

        chiselmon$rebuildTabButtons();

        Button.OnPress bookmarkToggle = (button) -> {
            PCTabStore tabStore = chiselmon$getTabStore();
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
            chiselmon$rebuildTabButtons();
        };

        int guiLeft = (this.width - BASE_WIDTH) / 2;
        int guiTop = (this.height - BASE_HEIGHT) / 2;

        int bookmarkX = guiLeft + 239;
        int bookmarkY = guiTop + 12;
        PCBookmarkButton bookmarkButton = new PCBookmarkButton(bookmarkX, bookmarkY, bookmarkToggle);
        this.chiselmon$bookmarkButton = bookmarkButton;
        this.addRenderableWidget(bookmarkButton);

        Button.OnPress homeToggle = (button) -> {
            storageWidget.setBox(0);
        };

        int homeX = guiLeft + 90;
        int homeY = guiTop + 12;
        PCHomeButton homeButton = new PCHomeButton(homeX, homeY, homeToggle);
        this.chiselmon$homeButton = homeButton;
        this.addRenderableWidget(homeButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void chiselmon$updateBookmarkButtonState(GuiGraphics context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        if (Chiselmon.isDisabled())
            return;
        ChiselmonConfig config = configService.get();
        if (!config.pc.bookmarksEnabled || this.chiselmon$bookmarkButton == null) {
            return;
        }
        PCTabStore tabStore = chiselmon$getTabStore();

        boolean isCurrentBoxBookmarked = tabStore.hasBoxNumber(storageWidget.getBox());
        this.chiselmon$bookmarkButton.setToggled(isCurrentBoxBookmarked);
    }

    @Unique
    private void chiselmon$rebuildTabButtons() {
        for (PCTabButton button : this.chiselmon$tabButtons) {
            this.removeWidget(button);
        }
        this.chiselmon$tabButtons.clear();

        PCTabStore tabStore = chiselmon$getTabStore();
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

        if (!newTabButtons.isEmpty()) {
            for (PCTabButton button : newTabButtons) {
                this.addRenderableWidget(button);
                this.chiselmon$tabButtons.add(button); // Track the new buttons with the list
            }
        }
    }
}
