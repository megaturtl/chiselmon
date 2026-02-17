package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.pc.PCUserData;
import cc.turtl.chiselmon.feature.pc.bookmark.BookmarkManager;
import cc.turtl.chiselmon.feature.pc.sort.SortManager;
import cc.turtl.chiselmon.userdata.UserDataRegistry;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
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
public abstract class MixinPCGUI extends Screen {

    @Shadow(remap = false)
    @Final
    public static int BASE_WIDTH;
    @Shadow(remap = false)
    @Final
    public static int BASE_HEIGHT;
    @Shadow
    @Final
    private ClientPC pc;
    @Shadow(remap = false)
    private StorageWidget storageWidget;
    @Shadow
    @Final
    private List<IconButton> optionButtons;
    @Shadow
    private boolean displayOptions;
    @Unique
    private BookmarkManager chiselmon$bookmarkManager;
    @Unique
    private SortManager chiselmon$sortManager;

    protected MixinPCGUI(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$initEntryPoint(CallbackInfo ci) {
        ChiselmonConfig config = ChiselmonConfig.get();
        if (config.general.modDisabled) return;

        int x = (width - BASE_WIDTH) / 2;
        int y = (height - BASE_HEIGHT) / 2;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        chiselmon$bookmarkManager = new BookmarkManager(
                UserDataRegistry.get(PCUserData.class).bookmarks,
                storageWidget,
                pc,
                this::addRenderableWidget,
                this::removeWidget
        );

        chiselmon$bookmarkManager.initialize(x, y);

        chiselmon$sortManager = new SortManager(pc, storageWidget, displayOptions, optionButtons, this::addRenderableWidget);
        chiselmon$sortManager.initialize(x, y);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void chiselmon$updateBookmarks(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chiselmon$bookmarkManager != null) {
            chiselmon$bookmarkManager.update();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void chiselmon$handleMiddleClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 2) return;
        if (chiselmon$sortManager == null) return;

        ChiselmonConfig config = ChiselmonConfig.get();
        if (!config.pc.quickSort.enabled) return;

        chiselmon$sortManager.executeQuickSort(config.pc.quickSort.mode, Screen.hasShiftDown());
    }

    @Override
    public void removed() {
        if (chiselmon$bookmarkManager != null) {
            chiselmon$bookmarkManager.cleanup();
            chiselmon$bookmarkManager = null;
        }

        chiselmon$sortManager = null;

        UserDataRegistry.save(PCUserData.class);

        super.removed();
    }
}