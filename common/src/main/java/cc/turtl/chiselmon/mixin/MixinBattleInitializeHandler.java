package cc.turtl.chiselmon.mixin;

import cc.turtl.chiselmon.client.api.BattleStartedEvent;
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.net.battle.BattleInitializeHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleInitializePacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleInitializeHandler.class, remap = false)
public class MixinBattleInitializeHandler {
    @Inject(
            method = "handle(Lcom/cobblemon/mod/common/net/messages/client/battle/BattleInitializePacket;Lnet/minecraft/client/Minecraft;)V",
            at = @At("TAIL")
    )
    // Pipe to the custom client side BattleStartedEvent
    private void chiselmon$onBattleInitialize(BattleInitializePacket packet, Minecraft client, CallbackInfo ci) {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle != null) {
            ChiselmonClientEvents.INSTANCE.getBATTLE_STARTED().invoke(new BattleStartedEvent(battle));
        }
    }
}