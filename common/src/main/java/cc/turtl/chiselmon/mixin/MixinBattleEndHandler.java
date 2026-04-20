package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.BattleEndedEvent;
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.net.battle.BattleEndHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleEndHandler.class, remap = false)
public class MixinBattleEndHandler {
    @Inject(
            method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleEndPacket;Lnet/minecraft/client/Minecraft;)V",
            at = @At("HEAD")
    )
    // Pipe to the custom client side BattleEndedEvent
    private void chiselmon$onBattleInitialize(BattleEndPacket packet, Minecraft client, CallbackInfo ci) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle != null) {
            ChiselmonClientEvents.INSTANCE.getBATTLE_ENDED().invoke(new BattleEndedEvent(battle));
        }
    }
}