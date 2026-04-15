package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.ChiselmonStorage;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.config.category.PCConfig;
import cc.turtl.chiselmon.client.feature.pc.sort.SortManager;
import cc.turtl.chiselmon.core.api.storage.Scope;
import cc.turtl.chiselmon.feature.pc.bookmark.BookmarkManager;
import cc.turtl.turtlshell.api.client.keybind.KeybindHelper;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import net.minecraft.client.gui.GuiGraphics;
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
public abstract class MixinPCGUI extends Screen {

    @Shadow(remap = false)
    @Final
    public static int BASE_WIDTH;
    @Shadow(remap = false)
    @Final
    public static int BASE_HEIGHT;
    @Shadow(remap = false)
    @Final
    private ClientPC pc;
    @Shadow(remap = false)
    private StorageWidget storageWidget;
    @Shadow(remap = false)
    @Final
    private List<IconButton> optionButtons;
    @Shadow(remap = false)
    private boolean displayOptions;

    @Unique
    private BookmarkManager chiselmon$bookmarkManager;
    @Unique
    private SortManager chiselmon$sortManager;

    protected MixinPCGUI(Component title) {
        super(title);
    }

    // Don't use remap=false here or InvMove early loading the class will break the Mixin injection
    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$init(CallbackInfo ci) {
        if (ChiselmonConfig.INSTANCE.getGeneral().getModDisabled()) return;

        Scope worldScope = Scope.Companion.currentWorld();
        if (worldScope == null) return;

        int x = (width - BASE_WIDTH) / 2;
        int y = (height - BASE_HEIGHT) / 2;

        chiselmon$bookmarkManager = new BookmarkManager(
                ChiselmonStorage.PC_SETTINGS.get(worldScope).bookmarks,
                storageWidget, pc,
                this::addRenderableWidget,
                this::removeWidget
        );
        chiselmon$bookmarkManager.initialize(x, y);

        chiselmon$sortManager = new SortManager(pc, storageWidget, displayOptions, optionButtons, this::addRenderableWidget);
        chiselmon$sortManager.initialize(x, y);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void chiselmon$render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chiselmon$bookmarkManager != null) chiselmon$bookmarkManager.update();

        PCConfig.QuickSortConfig quickSort = ChiselmonConfig.INSTANCE.getPc().getQuickSort();
        if (chiselmon$sortManager != null && quickSort.getEnabled() && KeybindHelper.INSTANCE.isDown(quickSort.getHotkey())) {
            chiselmon$sortManager.executeQuickSort(quickSort.getMode(), Screen.hasShiftDown());
        }
    }

    @Override
    public void removed() {
        if (chiselmon$bookmarkManager != null) {
            chiselmon$bookmarkManager.cleanup();
            chiselmon$bookmarkManager = null;
        }
        chiselmon$sortManager = null;

        Scope worldScope = Scope.Companion.currentWorld();
        if (worldScope != null) ChiselmonStorage.PC_SETTINGS.save(worldScope);

        super.removed();
    }
}