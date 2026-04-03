package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.config.category.PCConfig;
import cc.turtl.chiselmon.feature.eggspy.EggDummy;
import cc.turtl.chiselmon.feature.eggspy.EggRenderer;
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

    @Shadow(remap = false)
    public abstract Pokemon getPokemon();

    // `renderSlot` is only called if there is a pokemon. So we clear the tooltip here to prevent stale tooltips.
    @Inject(method = "renderWidget", at = @At("TAIL"))
    private void chiselmon$clearTooltip(GuiGraphics context, int MouseX, int mouseY, float delta, CallbackInfo ci) {
        if (getPokemon() == null) setTooltip(null);
    }

    @Inject(method = "renderSlot", at = @At("TAIL"), remap = false)
    private void chiselmon$renderTooltip(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        ChiselmonConfig config = ChiselmonConfig.INSTANCE;
        if (config.getGeneral().getModDisabled()) return;

        Pokemon pokemon = getPokemon();
        // Use preview so we either pass the eggdummy or normal pokemon
        Pokemon preview = ((DuckPreviewPokemon) pokemon).chiselmon$getPreview();

        if (config.getPc().getIcon().getEnabled() && preview != null) {
            IconRenderer.renderIcons(context, config.getPc().getIcon(), preview, posX, posY);
        }

        if (config.getGeneral().getEggSpy().getEnabled() && preview instanceof EggDummy eggDummy) {
            EggRenderer.renderStorageSlot(context, eggDummy, posX, posY);
        }

        if (config.getPc().getTooltip().getEnabled() && isHovered) {
            chiselmon$updateTooltip(preview, config.getPc().getTooltip());
        }
    }

    @Unique
    private void chiselmon$updateTooltip(Pokemon pokemon, PCConfig.TooltipConfig config) {
        boolean isShiftDown = Screen.hasShiftDown();
        boolean shouldShowTooltip = config.getShowOnHover() ||
                (config.getExtendOnShift() && isShiftDown);

        if (shouldShowTooltip && pokemon != null) {
            setTooltip(TooltipBuilder.build(pokemon, config, isShiftDown));
        } else {
            setTooltip(null);
        }
    }
}
