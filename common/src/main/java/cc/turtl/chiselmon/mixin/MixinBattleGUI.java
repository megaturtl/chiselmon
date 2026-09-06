package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.battle.MoveTooltipRenderer;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleGUI.class)
public abstract class MixinBattleGUI {
    @Shadow(remap = false)
    public abstract BattleActionSelection getCurrentActionSelection();

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void chiselmon$renderMoveTooltip(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MoveTooltipRenderer.render(context, getCurrentActionSelection(), mouseX, mouseY);
    }
}
