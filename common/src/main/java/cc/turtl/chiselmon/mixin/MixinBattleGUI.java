package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.feature.battle.MoveTooltipRenderer;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleGUI.class)
public abstract class MixinBattleGUI extends Screen {
    protected MixinBattleGUI(Component title) {
        super(title);
    }

    @Shadow(remap = false)
    public abstract BattleActionSelection getCurrentActionSelection();

    @Inject(method = "render", at = @At("TAIL"))
    private void chiselmon$renderMoveTooltip(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MoveTooltipRenderer.schedule(this, getCurrentActionSelection(), mouseX, mouseY);
    }
}
