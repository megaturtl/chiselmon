package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.BattleState;
import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleMessageHandler.class, remap = false)
public class MixinBattleMessageHandler {

    /**
     * Intercepts cobblemon battle messages and stores the current battle count in BattleState
     */
    @Inject(method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleMessagePacket;Lnet/minecraft/client/Minecraft;)V", at = @At("HEAD"))
    private void chiselmon$onBattleMessage(BattleMessagePacket packet, Minecraft client, CallbackInfo ci) {
        for (Component message : packet.getMessages()) {
            if (message.getContents() instanceof TranslatableContents tc && tc.getKey().equals("cobblemon.battle.turn")) {
                Object[] args = tc.getArgs();
                if (args.length > 0) {
                    try {
                        BattleState.INSTANCE.onTurnMessage(Integer.parseInt(args[0].toString().trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }
}