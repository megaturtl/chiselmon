package cc.turtl.chiselmon.mixin.eggpreview;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.module.feature.EggPreviewModule;
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
        EggPreviewModule module = Chiselmon.modules().getModule(EggPreviewModule.class);
        if (module != null) {
            module.onGuiInit((PCGUI) (Object) this);
        }
    }
}
