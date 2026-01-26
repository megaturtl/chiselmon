package cc.turtl.chiselmon.mixin.pc;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.feature.eggpreview.EggPreviewRenderer;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;
import cc.turtl.chiselmon.feature.pc.PcIconRenderer;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageSlot.class)
public abstract class StorageSlotMixin {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow(remap = false)
    public abstract boolean isHovered(int mouseX, int mouseY);

    @Inject(method = "renderSlot", at = @At("RETURN"), remap = false)
    private void chiselmon$renderCustomElements(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        if (Chiselmon.isDisabled()) {
            return;
        }
        ChiselmonConfig config = Chiselmon.services().config().get();
        Pokemon pokemon = getPokemon();

        if (config.eggPreview.enabled && pokemon instanceof NeoDaycareEggDummy) {
            EggPreviewRenderer.renderStorageSlotElements(context, (NeoDaycareEggDummy) pokemon, posX, posY);
        }

        PcIconRenderer.renderIconElements(context, pokemon, posX, posY);
    }
}
