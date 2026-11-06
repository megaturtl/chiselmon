package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.feature.eggspy.EggPreview;
import cc.turtl.chiselmon.client.feature.pc.PCScreenFeatures;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.gui.pc.IconButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    @Shadow(remap = false)
    private ModelWidget modelWidget;
    @Shadow(remap = false)
    private Pokemon previewPokemon;

    @Unique
    private PCScreenFeatures chiselmon$features;
    @Unique
    private boolean chiselmon$eggPreviewEnabled;

    protected MixinPCGUI(Component title) {
        super(title);
    }

    // Don't use remap=false here or InvMove early loading the class will break the Mixin injection
    @Inject(method = "init", at = @At("TAIL"))
    private void chiselmon$init(CallbackInfo ci) {
        var general = ChiselmonConfig.INSTANCE.getGeneral();
        boolean disabled = general.getModDisabled();
        chiselmon$eggPreviewEnabled = !disabled && general.getEggSpy().getEnabled();

        if (chiselmon$features != null) chiselmon$features.cleanup();
        chiselmon$features = new PCScreenFeatures(
                pc, storageWidget, optionButtons,
                (width - BASE_WIDTH) / 2, (height - BASE_HEIGHT) / 2,
                this::addRenderableWidget, this::chiselmon$removeFeatureWidget
        );
        chiselmon$features.initialize(disabled, displayOptions);
    }

    @Unique
    private void chiselmon$removeFeatureWidget(GuiEventListener widget) {
        if (getFocused() == widget) clearFocus();
        removeWidget(widget);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void chiselmon$render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var general = ChiselmonConfig.INSTANCE.getGeneral();
        boolean disabled = general.getModDisabled();
        boolean eggPreviewEnabled = !disabled && general.getEggSpy().getEnabled();
        if (chiselmon$eggPreviewEnabled != eggPreviewEnabled) {
            chiselmon$eggPreviewEnabled = eggPreviewEnabled;
            if (modelWidget != null && previewPokemon != null) {
                modelWidget.setPokemon(EggPreview.renderableFor(previewPokemon));
            }
        }

        if (chiselmon$features != null) chiselmon$features.update(disabled, displayOptions);
    }

    @ModifyExpressionValue(
            method = "setPreviewPokemon",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;asRenderablePokemon()Lcom/cobblemon/mod/common/pokemon/RenderablePokemon;", remap = false),
            remap = false
    )
    private RenderablePokemon chiselmon$swapPreviewRenderable(RenderablePokemon original, @Local(argsOnly = true) Pokemon pokemon) {
        return pokemon == null ? original : EggPreview.renderableFor(pokemon);
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getDisplayName$default(Lcom/cobblemon/mod/common/pokemon/Pokemon;ZILjava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", remap = false)
    )
    private MutableComponent chiselmon$swapDisplayNameForDisplay(MutableComponent original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getDisplayName(false);
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getGender()Lcom/cobblemon/mod/common/pokemon/Gender;", remap = false)
    )
    private Gender chiselmon$swapGenderForDisplay(Gender original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getGender();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getIvs()Lcom/cobblemon/mod/common/pokemon/IVs;", remap = false)
    )
    private IVs chiselmon$swapIvsForDisplay(IVs original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getIvs();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getAbility()Lcom/cobblemon/mod/common/api/abilities/Ability;", remap = false)
    )
    private Ability chiselmon$swapAbilityForDisplay(Ability original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getAbility();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getPrimaryType()Lcom/cobblemon/mod/common/api/types/ElementalType;", remap = false)
    )
    private ElementalType chiselmon$swapPrimaryTypeForDisplay(ElementalType original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getPrimaryType();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getSecondaryType()Lcom/cobblemon/mod/common/api/types/ElementalType;", remap = false)
    )
    private ElementalType chiselmon$swapSecondaryTypeForDisplay(ElementalType original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getSecondaryType();
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/pokemon/Pokemon;getMoveSet()Lcom/cobblemon/mod/common/api/moves/MoveSet;", remap = false)
    )
    private MoveSet chiselmon$swapMoveSetForDisplay(MoveSet original, @Local(name = "pokemon") Pokemon pokemon) {
        Pokemon preview = EggPreview.forDisplay(pokemon);
        return preview == pokemon ? original : preview.getMoveSet();
    }

    @Override
    public void removed() {
        if (chiselmon$features != null) chiselmon$features.removed();

        super.removed();
    }
}