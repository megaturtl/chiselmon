package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.PCConfig;
import cc.turtl.chiselmon.feature.pc.eggspy.EggCache;
import cc.turtl.chiselmon.feature.pc.eggspy.EggDummy;
import cc.turtl.chiselmon.feature.pc.eggspy.EggRenderer;
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
    private void chiselmon$renderEntryPoint(GuiGraphics context, int posX, int posY, float delta, CallbackInfo ci) {
        ChiselmonConfig config = ChiselmonConfig.get();
        if (config.general.modDisabled) return;

        Pokemon pokemon = getPokemon();
        // Use preview so we either pass the eggdummy or normal pokemon
        Pokemon preview = ((DuckPreviewPokemon) pokemon).chiselmon$getPreview();

        if (config.pc.icon.enabled && preview != null) {
            IconRenderer.renderIcons(context, config.pc.icon, preview, posX, posY);
        }

        if (config.pc.eggSpy.enabled && preview instanceof EggDummy eggDummy) {
            EggRenderer.renderStorageSlot(context, eggDummy, posX, posY);
        }

        if (config.pc.tooltip.enabled) {
            chiselmon$updateTooltip(preview, config.pc.tooltip);
        }
    }

    @Unique
    private void chiselmon$updateTooltip(Pokemon pokemon, PCConfig.TooltipConfig config) {
        if (!isHovered()) {
            setTooltip(null);
            return;
        }

        boolean isShiftDown = Screen.hasShiftDown();
        boolean shouldShowTooltip = config.showOnHover ||
                (config.extendOnShift && isShiftDown);

        if (shouldShowTooltip && pokemon != null) {
            setTooltip(TooltipBuilder.build(pokemon, config, isShiftDown));
        } else {
            setTooltip(null);
        }
    }
}
