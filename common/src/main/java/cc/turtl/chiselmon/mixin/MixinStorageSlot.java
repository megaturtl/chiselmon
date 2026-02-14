package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.OLDPCConfig;
import cc.turtl.chiselmon.feature.pc.eggpreview.EggDummy;
import cc.turtl.chiselmon.feature.pc.eggpreview.EggRenderer;
import cc.turtl.chiselmon.feature.pc.icon.IconRenderer;
import cc.turtl.chiselmon.feature.pc.tooltip.TooltipBuilder;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageSlot.class)
public abstract class MixinStorageSlot extends AbstractWidget {

    // Dummy constructor for extending AbstractWidget (not used at runtime)
    private MixinStorageSlot(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Shadow
    public abstract Pokemon getPokemon();

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void chiselmon$renderCustom(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        Pokemon pokemon = this.getPokemon();
        if (pokemon != null) {

            IconRenderer.renderIcons(context, pokemon, posX, posY);

            if (pokemon instanceof EggDummy) {
                EggRenderer.renderStorageSlot(context, (EggDummy) pokemon, posX, posY);
            }
        }

        chiselmon$setTooltip();
    }

    @Unique
    private void chiselmon$setTooltip() {

        OLDPCConfig.PCTooltipConfig config = ChiselmonConstants.CONFIG.pc.tooltip;

        boolean shouldAdd = this.isHovered() && (config.showOnHover ||
                (config.extendOnShift && Screen.hasShiftDown()));

        if (shouldAdd) {
            Pokemon pokemon = this.getPokemon();
            if (pokemon != null) {
                this.setTooltip(TooltipBuilder.buildAndSet(pokemon, Screen.hasShiftDown()));
            }
        } else {
            this.setTooltip(null);
        }
    }
}
