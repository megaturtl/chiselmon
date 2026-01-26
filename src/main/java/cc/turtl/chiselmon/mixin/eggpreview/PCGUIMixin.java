package cc.turtl.chiselmon.mixin.eggpreview;

import cc.turtl.chiselmon.feature.eggpreview.EggPreviewManager;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    protected PCGUIMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$refreshEggs(CallbackInfo ci) {
        EggPreviewManager.refreshPartyEggs((PCGUI) (Object) this);
    }
}
