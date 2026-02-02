package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.leveldata.LevelDataHelper;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.pc.bookmark.BookmarkManager;
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

/**
 * Clean mixin using callbacks for widget management.
 * No reflection, no instanceof checks - just simple delegation.
 */
@Mixin(PCGUI.class)
public abstract class MixinPCGUI extends Screen {

    protected MixinPCGUI(Component title) {
        super(title);
    }

    @Shadow @Final
    private ClientPC pc;
    @Shadow(remap = false) private StorageWidget storageWidget;
    @Shadow(remap = false) @Final public static int BASE_WIDTH;
    @Shadow(remap = false) @Final public static int BASE_HEIGHT;

    @Unique private BookmarkManager chiselmon$bookmarkManager;

    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$initBookmarks(CallbackInfo ci) {
        if (ChiselmonConstants.CONFIG.modDisabled) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        chiselmon$bookmarkManager = new BookmarkManager(
                LevelDataHelper.getLevelData(level).bookmarkStore,
                storageWidget,
                pc,
                this::addRenderableWidget,  // Method reference - clean!
                this::removeWidget           // Method reference - clean!
        );

        chiselmon$bookmarkManager.initialize(
                (width - BASE_WIDTH) / 2,
                (height - BASE_HEIGHT) / 2
        );
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void chiselmon$updateBookmarks(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (chiselmon$bookmarkManager != null) {
            chiselmon$bookmarkManager.update();
        }
    }

    @Inject(method = "closeNormally", at = @At("HEAD"))
    private void chiselmon$cleanupBookmarks(CallbackInfo ci) {
        if (chiselmon$bookmarkManager != null) {
            chiselmon$bookmarkManager.cleanup();
            chiselmon$bookmarkManager = null;
        }
    }
}