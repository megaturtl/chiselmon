package cc.turtl.chiselmon.mixin.pc;

import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.compat.neodaycare.NeoDaycareEggDummy;
import cc.turtl.chiselmon.config.ModConfig;
import cc.turtl.chiselmon.feature.pc.PcEggRenderer;
import cc.turtl.chiselmon.feature.pc.PcIconRenderer;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

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
        ModConfig config = Chiselmon.services().config().get();
        Pokemon pokemon = getPokemon();

        if (config.pc.showEggPreview && pokemon instanceof NeoDaycareEggDummy) {
            PcEggRenderer.renderEggPreviewElements(context, (NeoDaycareEggDummy) pokemon, posX, posY);
        }

        PcIconRenderer.renderIconElements(context, pokemon, posX, posY);
    }
}
